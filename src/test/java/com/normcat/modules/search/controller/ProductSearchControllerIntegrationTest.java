package com.normcat.modules.search.controller;

import com.normcat.modules.category.entity.Category;
import com.normcat.modules.category.repository.CategoryRepository;
import com.normcat.modules.dictionary.entity.Attribute;
import com.normcat.modules.dictionary.entity.AttributeDataType;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.dictionary.repository.AttributeRepository;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
import com.normcat.modules.product.entity.Product;
import com.normcat.modules.product.entity.ProductAttributeValue;
import com.normcat.modules.product.entity.ProductNormalizationStatus;
import com.normcat.modules.product.entity.ProductStatus;
import com.normcat.modules.product.repository.ProductAttributeValueRepository;
import com.normcat.modules.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductSearchControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("normcat_search_test_db")
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
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private AttributeValueRepository attributeValueRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductAttributeValueRepository productAttributeValueRepository;

    private Category shirtCategory;
    private Category jeanCategory;
    private Attribute colorAttr;
    private Attribute sizeAttr;
    private AttributeValue redVal;
    private AttributeValue blueVal;
    private AttributeValue xlVal;
    private AttributeValue lVal;

    private Product p1RedXl;
    private Product p2RedL;
    private Product p3BlueXl;
    private Product p4Inactive;

    @BeforeEach
    void setUp() {
        productAttributeValueRepository.deleteAll();
        productRepository.deleteAll();
        attributeValueRepository.deleteAll();
        attributeRepository.deleteAll();
        categoryRepository.deleteAll();

        // 1. Categories
        shirtCategory = categoryRepository.save(Category.builder()
                .name("Áo sơ mi nam")
                .slug("ao-so-mi-nam")
                .isActive(true)
                .build());

        jeanCategory = categoryRepository.save(Category.builder()
                .name("Quần jean nam")
                .slug("quan-jean-nam")
                .isActive(true)
                .build());

        // 2. Attributes
        colorAttr = attributeRepository.save(Attribute.builder()
                .code("COLOR")
                .name("Màu sắc")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build());

        sizeAttr = attributeRepository.save(Attribute.builder()
                .code("SIZE")
                .name("Kích cỡ")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build());

        // 3. Values
        redVal = attributeValueRepository.save(AttributeValue.builder()
                .attribute(colorAttr)
                .code("RED")
                .displayValue("Đỏ")
                .sortOrder(1)
                .build());

        blueVal = attributeValueRepository.save(AttributeValue.builder()
                .attribute(colorAttr)
                .code("BLUE")
                .displayValue("Xanh dương")
                .sortOrder(2)
                .build());

        xlVal = attributeValueRepository.save(AttributeValue.builder()
                .attribute(sizeAttr)
                .code("XL")
                .displayValue("XL")
                .sortOrder(1)
                .build());

        lVal = attributeValueRepository.save(AttributeValue.builder()
                .attribute(sizeAttr)
                .code("L")
                .displayValue("L")
                .sortOrder(2)
                .build());

        // 4. Products
        // P1: Red XL, Shirt, 199,000
        p1RedXl = productRepository.save(Product.builder()
                .sku("TSHIRT-RED-XL")
                .name("Áo Thun Cotton Cổ Tròn Đỏ XL")
                .category(shirtCategory)
                .price(new BigDecimal("199000.00"))
                .status(ProductStatus.ACTIVE)
                .normalizationStatus(ProductNormalizationStatus.NORMALIZED)
                .rawAttributes(Map.of("Màu", "Đỏ", "Size", "XL"))
                .build());

        linkPav(p1RedXl, colorAttr, redVal);
        linkPav(p1RedXl, sizeAttr, xlVal);

        // P2: Red L, Shirt, 299,000
        p2RedL = productRepository.save(Product.builder()
                .sku("TSHIRT-RED-L")
                .name("Áo Sơ Mi Oxford Đỏ L")
                .category(shirtCategory)
                .price(new BigDecimal("299000.00"))
                .status(ProductStatus.ACTIVE)
                .normalizationStatus(ProductNormalizationStatus.NORMALIZED)
                .rawAttributes(Map.of("Màu", "Đỏ", "Size", "L"))
                .build());

        linkPav(p2RedL, colorAttr, redVal);
        linkPav(p2RedL, sizeAttr, lVal);

        // P3: Blue XL, Jean, 399,000
        p3BlueXl = productRepository.save(Product.builder()
                .sku("JEAN-BLUE-XL")
                .name("Quần Jean Slimfit Xanh XL")
                .category(jeanCategory)
                .price(new BigDecimal("399000.00"))
                .status(ProductStatus.ACTIVE)
                .normalizationStatus(ProductNormalizationStatus.NORMALIZED)
                .rawAttributes(Map.of("Màu", "Xanh", "Size", "XL"))
                .build());

        linkPav(p3BlueXl, colorAttr, blueVal);
        linkPav(p3BlueXl, sizeAttr, xlVal);

        // P4: Inactive product (should never appear in search)
        p4Inactive = productRepository.save(Product.builder()
                .sku("INACTIVE-ITEM")
                .name("Sản phẩm ngưng bán")
                .category(shirtCategory)
                .price(new BigDecimal("99000.00"))
                .status(ProductStatus.INACTIVE)
                .normalizationStatus(ProductNormalizationStatus.NORMALIZED)
                .rawAttributes(Map.of())
                .build());
    }

    private void linkPav(Product product, Attribute attribute, AttributeValue attributeValue) {
        productAttributeValueRepository.save(ProductAttributeValue.builder()
                .product(product)
                .attributeId(attribute.getId())
                .attributeValue(attributeValue)
                .build());
    }

    @Test
    @DisplayName("Should return all active products when no filters applied")
    void shouldReturnAllActiveProductsWhenNoFiltersApplied() throws Exception {
        mockMvc.perform(get("/api/v1/search/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.isLast").value(true));
    }

    @Test
    @DisplayName("Should filter products by keyword matching name or SKU")
    void shouldFilterProductsByKeyword() throws Exception {
        // Keyword matching name
        mockMvc.perform(get("/api/v1/search/products")
                        .param("keyword", "oxford")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("TSHIRT-RED-L"))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("Áo sơ mi nam"));

        // Keyword matching SKU
        mockMvc.perform(get("/api/v1/search/products")
                        .param("keyword", "jean-blue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("JEAN-BLUE-XL"));
    }

    @Test
    @DisplayName("Should filter products by category slug")
    void shouldFilterProductsByCategorySlug() throws Exception {
        mockMvc.perform(get("/api/v1/search/products")
                        .param("categorySlug", "quan-jean-nam")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("JEAN-BLUE-XL"));
    }

    @Test
    @DisplayName("Should filter products by minPrice and maxPrice")
    void shouldFilterProductsByPriceRange() throws Exception {
        mockMvc.perform(get("/api/v1/search/products")
                        .param("minPrice", "200000")
                        .param("maxPrice", "350000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("TSHIRT-RED-L"));
    }

    @Test
    @DisplayName("Should filter products by single attribute facet")
    void shouldFilterProductsBySingleFacet() throws Exception {
        mockMvc.perform(get("/api/v1/search/products")
                        .param("attrs[COLOR]", "RED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[*].sku", containsInAnyOrder("TSHIRT-RED-XL", "TSHIRT-RED-L")))
                .andExpect(jsonPath("$.data.content[0].normalizedAttributes.COLOR").value("Đỏ"));
    }

    @Test
    @DisplayName("Should filter products by multiple attribute facets using AND logic")
    void shouldFilterProductsByMultipleFacetsWithAndLogic() throws Exception {
        // COLOR=RED AND SIZE=XL should match ONLY P1 (TSHIRT-RED-XL)
        mockMvc.perform(get("/api/v1/search/products")
                        .param("attrs[COLOR]", "RED")
                        .param("attrs[SIZE]", "XL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("TSHIRT-RED-XL"))
                .andExpect(jsonPath("$.data.content[0].normalizedAttributes.COLOR").value("Đỏ"))
                .andExpect(jsonPath("$.data.content[0].normalizedAttributes.SIZE").value("XL"));
    }

    @Test
    @DisplayName("Should return empty list when facet does not match any product")
    void shouldReturnEmptyWhenFacetDoesNotMatch() throws Exception {
        // BLUE and L: No product has both BLUE and L
        mockMvc.perform(get("/api/v1/search/products")
                        .param("attrs[COLOR]", "BLUE")
                        .param("attrs[SIZE]", "L")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should correctly paginate and sort products")
    void shouldPaginateAndSortProducts() throws Exception {
        // Sort by price ascending, size = 1, page = 0 -> Cheapest item: P1 (199,000)
        mockMvc.perform(get("/api/v1/search/products")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "price,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.isLast").value(false))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].sku").value("TSHIRT-RED-XL"));

        // Page 1 -> Next cheapest item: P2 (299,000)
        mockMvc.perform(get("/api/v1/search/products")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "price,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.content[0].sku").value("TSHIRT-RED-L"));

        // Page 2 -> Most expensive item: P3 (399,000), isLast = true
        mockMvc.perform(get("/api/v1/search/products")
                        .param("page", "2")
                        .param("size", "1")
                        .param("sort", "price,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.isLast").value(true))
                .andExpect(jsonPath("$.data.content[0].sku").value("JEAN-BLUE-XL"));
    }
}
