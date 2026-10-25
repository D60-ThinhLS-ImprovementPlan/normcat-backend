package com.normcat.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("normcat_test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should successfully apply Flyway migration V1 and create all 7 tables")
    void shouldApplyFlywayMigrationSuccessfully() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' " +
                        "AND table_name IN ('categories', 'attributes', 'attribute_values', " +
                        "'attribute_aliases', 'attribute_value_aliases', 'products', 'product_attribute_values')",
                Integer.class);

        assertThat(tableCount).isEqualTo(7);
    }

    @Test
    @DisplayName("Should enforce composite foreign key between attribute_value_aliases and attribute_values")
    void shouldEnforceCompositeForeignKeyOnValueAliases() {
        // Insert standard attribute: COLOR
        jdbcTemplate.update("INSERT INTO attributes (code, name, is_multi_valued) VALUES ('COLOR', 'Màu sắc', TRUE)");
        Long colorId = jdbcTemplate.queryForObject("SELECT id FROM attributes WHERE code = 'COLOR'", Long.class);

        // Insert standard value: RED under COLOR
        jdbcTemplate.update("INSERT INTO attribute_values (attribute_id, code, display_value) VALUES (?, 'RED', 'Đỏ')", colorId);
        Long redValId = jdbcTemplate.queryForObject("SELECT id FROM attribute_values WHERE code = 'RED'", Long.class);

        // Insert standard attribute: SIZE
        jdbcTemplate.update("INSERT INTO attributes (code, name, is_multi_valued) VALUES ('SIZE', 'Kích cỡ', FALSE)");
        Long sizeId = jdbcTemplate.queryForObject("SELECT id FROM attributes WHERE code = 'SIZE'", Long.class);

        // Valid alias: attribute_id = colorId and attribute_value_id = redValId
        jdbcTemplate.update("INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES (?, ?, 'đỏ', 'đỏ')",
                colorId, redValId);

        // Invalid alias: attribute_id = sizeId (mismatched attribute) but attribute_value_id = redValId
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES (?, ?, 'large', 'large')",
                sizeId, redValId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should enforce unique constraint on normalized_name in attribute_aliases")
    void shouldEnforceUniqueConstraintOnNormalizedAttributeAliases() {
        jdbcTemplate.update("INSERT INTO attributes (code, name) VALUES ('BRAND', 'Thương hiệu')");
        Long brandId = jdbcTemplate.queryForObject("SELECT id FROM attributes WHERE code = 'BRAND'", Long.class);

        // Insert first alias with normalized_name = 'nhan hieu'
        jdbcTemplate.update("INSERT INTO attribute_aliases (attribute_id, raw_attribute_name, normalized_name) VALUES (?, 'nhãn hiệu', 'nhan hieu')", brandId);

        // Insert duplicate normalized_name must be rejected
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO attribute_aliases (attribute_id, raw_attribute_name, normalized_name) VALUES (?, '  NHÃN HIỆU  ', 'nhan hieu')", brandId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should enforce unique constraint on (attribute_id, normalized_value) in attribute_value_aliases")
    void shouldEnforceUniqueConstraintOnNormalizedValueAliases() {
        jdbcTemplate.update("INSERT INTO attributes (code, name) VALUES ('RAM', 'Bộ nhớ RAM')");
        Long ramId = jdbcTemplate.queryForObject("SELECT id FROM attributes WHERE code = 'RAM'", Long.class);

        jdbcTemplate.update("INSERT INTO attribute_values (attribute_id, code, display_value) VALUES (?, 'RAM_16GB', '16 GB')", ramId);
        Long ramValId = jdbcTemplate.queryForObject("SELECT id FROM attribute_values WHERE code = 'RAM_16GB'", Long.class);

        // First value alias
        jdbcTemplate.update("INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES (?, ?, '16 gb', '16gb')",
                ramId, ramValId);

        // Duplicate normalized_value under the same attribute_id must be rejected
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES (?, ?, '16  GB', '16gb')",
                ramId, ramValId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should enforce CHECK constraint on products.price >= 0")
    void shouldEnforceCheckConstraintOnProductPrice() {
        jdbcTemplate.update("INSERT INTO categories (name, slug) VALUES ('Thời trang', 'thoi-trang')");
        Long catId = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE slug = 'thoi-trang'", Long.class);

        // Negative price must be rejected
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO products (sku, name, category_id, price) VALUES ('TEST-SKU-1', 'Test Product', ?, -10.00)",
                catId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should successfully insert product with default PENDING normalization status")
    void shouldAllowDefaultPendingNormalizationStatus() {
        jdbcTemplate.update("INSERT INTO categories (name, slug) VALUES ('Điện tử', 'dien-tu')");
        Long catId = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE slug = 'dien-tu'", Long.class);

        // Insert product omitting normalization_status to verify DEFAULT 'PENDING' passes CHECK constraint
        jdbcTemplate.update(
                "INSERT INTO products (sku, name, category_id, price) VALUES ('SKU-PENDING-1', 'Pending Product', ?, 199.99)",
                catId);

        String status = jdbcTemplate.queryForObject(
                "SELECT normalization_status FROM products WHERE sku = 'SKU-PENDING-1'", String.class);

        assertThat(status).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Should enforce composite foreign key between product_attribute_values and attribute_values")
    void shouldEnforceCompositeForeignKeyOnProductAttributeValues() {
        // Setup category and product
        jdbcTemplate.update("INSERT INTO categories (name, slug) VALUES ('Gia dụng', 'gia-dung')");
        Long catId = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE slug = 'gia-dung'", Long.class);
        jdbcTemplate.update(
                "INSERT INTO products (sku, name, category_id, price) VALUES ('SKU-PAV-1', 'Smart Kettle', ?, 49.99)",
                catId);
        Long prodId = jdbcTemplate.queryForObject("SELECT id FROM products WHERE sku = 'SKU-PAV-1'", Long.class);

        // Setup attributes and values: COLOR -> WHITE, MATERIAL -> STEEL
        jdbcTemplate.update("INSERT INTO attributes (code, name, is_multi_valued) VALUES ('COLOR_2', 'Màu', TRUE)");
        Long colorId = jdbcTemplate.queryForObject("SELECT id FROM attributes WHERE code = 'COLOR_2'", Long.class);
        jdbcTemplate.update("INSERT INTO attribute_values (attribute_id, code, display_value) VALUES (?, 'WHITE', 'Trắng')", colorId);
        Long whiteValId = jdbcTemplate.queryForObject("SELECT id FROM attribute_values WHERE code = 'WHITE'", Long.class);

        jdbcTemplate.update("INSERT INTO attributes (code, name, is_multi_valued) VALUES ('MATERIAL', 'Chất liệu', FALSE)");
        Long materialId = jdbcTemplate.queryForObject("SELECT id FROM attributes WHERE code = 'MATERIAL'", Long.class);

        // Valid: prodId, colorId, whiteValId (whiteValId belongs to colorId)
        jdbcTemplate.update(
                "INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES (?, ?, ?)",
                prodId, colorId, whiteValId);

        // Invalid: prodId, materialId (mismatched!), whiteValId (belongs to colorId)
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES (?, ?, ?)",
                prodId, materialId, whiteValId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
