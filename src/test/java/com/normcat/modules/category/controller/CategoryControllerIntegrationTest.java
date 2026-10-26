package com.normcat.modules.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.normcat.modules.category.dto.request.CreateCategoryRequest;
import com.normcat.modules.category.repository.CategoryRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CategoryControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("normcat_category_test_db")
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

    @BeforeEach
    void cleanUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/categories - should create root category with status 201")
    void shouldCreateRootCategorySuccessfully() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Thời trang nam")
                .slug("thoi-trang-nam")
                .description("Men fashion category")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Category created successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.name", is("Thời trang nam")))
                .andExpect(jsonPath("$.data.slug", is("thoi-trang-nam")))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.isActive", is(true)));
    }

    @Test
    @DisplayName("POST /api/v1/categories - should create child category when parentId exists")
    void shouldCreateChildCategorySuccessfully() throws Exception {
        CreateCategoryRequest parentReq = CreateCategoryRequest.builder()
                .name("Điện tử")
                .slug("dien-tu")
                .build();

        String parentResStr = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number parentId = com.jayway.jsonpath.JsonPath.read(parentResStr, "$.data.id");

        CreateCategoryRequest childReq = CreateCategoryRequest.builder()
                .name("Điện thoại")
                .slug("dien-thoai")
                .parentId(parentId.longValue())
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(childReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.slug", is("dien-thoai")))
                .andExpect(jsonPath("$.data.parentId", is(parentId.intValue())));
    }

    @Test
    @DisplayName("POST /api/v1/categories - should return 409 Conflict when slug already exists")
    void shouldReturn409WhenSlugAlreadyExists() throws Exception {
        CreateCategoryRequest firstReq = CreateCategoryRequest.builder()
                .name("Gia dụng")
                .slug("gia-dung")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstReq)))
                .andExpect(status().isCreated());

        CreateCategoryRequest duplicateReq = CreateCategoryRequest.builder()
                .name("Gia dụng nhà bếp")
                .slug("gia-dung")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/v1/categories - should return 404 Not Found when parentId does not exist")
    void shouldReturn404WhenParentNotFound() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Laptop")
                .slug("laptop")
                .parentId(99999L)
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Parent category not found")));
    }

    @Test
    @DisplayName("POST /api/v1/categories - should return 400 Bad Request when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("") // blank name
                .slug("INVALID SLUG WITH SPACES") // invalid regex
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/v1/categories/tree - should return hierarchical tree structure")
    void shouldReturnHierarchicalCategoryTree() throws Exception {
        // Create Root 1
        String root1Res = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateCategoryRequest.builder()
                                .name("Thời trang")
                                .slug("thoi-trang")
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number root1Id = com.jayway.jsonpath.JsonPath.read(root1Res, "$.data.id");

        // Create Child 1 of Root 1
        String child1Res = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateCategoryRequest.builder()
                                .name("Áo nam")
                                .slug("ao-nam")
                                .parentId(root1Id.longValue())
                                .build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number child1Id = com.jayway.jsonpath.JsonPath.read(child1Res, "$.data.id");

        // Create Sub-child of Child 1
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateCategoryRequest.builder()
                                .name("Áo thun")
                                .slug("ao-thun")
                                .parentId(child1Id.longValue())
                                .build())))
                .andExpect(status().isCreated());

        // Get Tree
        mockMvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].slug", is("thoi-trang")))
                .andExpect(jsonPath("$.data[0].children", hasSize(1)))
                .andExpect(jsonPath("$.data[0].children[0].slug", is("ao-nam")))
                .andExpect(jsonPath("$.data[0].children[0].children", hasSize(1)))
                .andExpect(jsonPath("$.data[0].children[0].children[0].slug", is("ao-thun")));
    }
}
