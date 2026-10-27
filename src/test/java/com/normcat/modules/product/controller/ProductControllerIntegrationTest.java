package com.normcat.modules.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.normcat.modules.category.entity.Category;
import com.normcat.modules.category.repository.CategoryRepository;
import com.normcat.modules.dictionary.entity.Attribute;
import com.normcat.modules.dictionary.entity.AttributeAlias;
import com.normcat.modules.dictionary.entity.AttributeDataType;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.dictionary.entity.AttributeValueAlias;
import com.normcat.modules.dictionary.repository.AttributeAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeRepository;
import com.normcat.modules.dictionary.repository.AttributeValueAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
import com.normcat.modules.product.dto.request.CreateProductRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("normcat_product_test_db")
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
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private AttributeValueRepository attributeValueRepository;

    @Autowired
    private AttributeAliasRepository attributeAliasRepository;

    @Autowired
    private AttributeValueAliasRepository attributeValueAliasRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductAttributeValueRepository productAttributeValueRepository;

    private Category savedCategory;

    @BeforeEach
    void setUp() {
        productAttributeValueRepository.deleteAll();
        productRepository.deleteAll();
        attributeValueAliasRepository.deleteAll();
        attributeAliasRepository.deleteAll();
        attributeValueRepository.deleteAll();
        attributeRepository.deleteAll();
        categoryRepository.deleteAll();

        // 1. Seed Category
        savedCategory = categoryRepository.save(Category.builder()
                .name("Điện thoại")
                .slug("dien-thoai")
                .isActive(true)
                .build());

        // 2. Seed Attribute: COLOR
        Attribute colorAttr = attributeRepository.save(Attribute.builder()
                .code("COLOR")
                .name("Màu sắc")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build());

        AttributeValue redVal = attributeValueRepository.save(AttributeValue.builder()
                .attribute(colorAttr)
                .code("RED")
                .displayValue("Đỏ")
                .sortOrder(1)
                .build());

        attributeAliasRepository.save(AttributeAlias.builder()
                .attribute(colorAttr)
                .rawAttributeName("Màu sắc")
                .normalizedName("màu sắc")
                .build());

        attributeValueAliasRepository.save(AttributeValueAlias.builder()
                .attributeId(colorAttr.getId())
                .attributeValue(redVal)
                .rawValue("Đỏ")
                .normalizedValue("đỏ")
                .build());

        // 3. Seed Attribute: RAM
        Attribute ramAttr = attributeRepository.save(Attribute.builder()
                .code("RAM")
                .name("Bộ nhớ RAM")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build());

        AttributeValue ram8gVal = attributeValueRepository.save(AttributeValue.builder()
                .attribute(ramAttr)
                .code("8GB")
                .displayValue("8 GB")
                .sortOrder(1)
                .build());

        attributeAliasRepository.save(AttributeAlias.builder()
                .attribute(ramAttr)
                .rawAttributeName("Bộ nhớ")
                .normalizedName("bộ nhớ")
                .build());

        attributeValueAliasRepository.save(AttributeValueAlias.builder()
                .attributeId(ramAttr.getId())
                .attributeValue(ram8gVal)
                .rawValue("8G")
                .normalizedValue("8g")
                .build());
    }

    @Test
    @DisplayName("Should ingest product with dual storage: raw JSONB and relational normalized attributes")
    void ingestProduct_success_dualStorage() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("IPHONE-15-PRO-128")
                .name("Apple iPhone 15 Pro 128GB")
                .categoryId(savedCategory.getId())
                .price(new BigDecimal("25990000.00"))
                .status(ProductStatus.ACTIVE)
                .rawAttributes(Map.of(
                        "Màu sắc", "Đỏ",
                        "Bộ nhớ", "8G",
                        "Xuất xứ", "Việt Nam" // Unknown attribute
                ))
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.sku", is("IPHONE-15-PRO-128")))
                .andExpect(jsonPath("$.data.normalizationStatus", is("PARTIALLY_NORMALIZED")))
                .andExpect(jsonPath("$.data.rawAttributes['Xuất xứ']", is("Việt Nam")))
                .andExpect(jsonPath("$.data.rawAttributes['Màu sắc']", is("Đỏ")))
                .andExpect(jsonPath("$.data.rawAttributes['Bộ nhớ']", is("8G")))
                .andExpect(jsonPath("$.data.normalizedAttributes", hasSize(2)))
                .andExpect(jsonPath("$.data.normalizedAttributes[*].attributeCode", containsInAnyOrder("COLOR", "RAM")))
                .andExpect(jsonPath("$.data.normalizedAttributes[*].attributeValueCode", containsInAnyOrder("RED", "8GB")));
    }

    @Test
    @DisplayName("Should return 409 Conflict when ingesting product with duplicate SKU")
    void ingestProduct_duplicateSku_returns409() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("DUP-SKU-001")
                .name("Product Original")
                .categoryId(savedCategory.getId())
                .price(BigDecimal.valueOf(100))
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")));
    }

    @Test
    @DisplayName("Should retrieve product details by ID and SKU")
    void getProduct_byIdAndSku_success() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("GET-PROD-001")
                .name("Retrievable Product")
                .categoryId(savedCategory.getId())
                .price(BigDecimal.valueOf(500))
                .rawAttributes(Map.of("Bộ nhớ", "8G"))
                .build();

        String responseContent = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number productId = com.jayway.jsonpath.JsonPath.read(responseContent, "$.data.id");

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku", is("GET-PROD-001")))
                .andExpect(jsonPath("$.data.normalizedAttributes", hasSize(1)));

        mockMvc.perform(get("/api/v1/products/sku/{sku}", "GET-PROD-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(productId)));
    }
}
