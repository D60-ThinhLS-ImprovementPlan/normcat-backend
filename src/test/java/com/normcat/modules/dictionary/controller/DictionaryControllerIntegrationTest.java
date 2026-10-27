package com.normcat.modules.dictionary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.normcat.modules.dictionary.dto.request.CreateAttributeAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueRequest;
import com.normcat.modules.dictionary.entity.AttributeDataType;
import com.normcat.modules.dictionary.repository.AttributeAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeRepository;
import com.normcat.modules.dictionary.repository.AttributeValueAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
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
class DictionaryControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("normcat_dictionary_test_db")
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
    private AttributeRepository attributeRepository;

    @Autowired
    private AttributeValueRepository attributeValueRepository;

    @Autowired
    private AttributeAliasRepository attributeAliasRepository;

    @Autowired
    private AttributeValueAliasRepository attributeValueAliasRepository;

    @BeforeEach
    void cleanUp() {
        attributeValueAliasRepository.deleteAll();
        attributeAliasRepository.deleteAll();
        attributeValueRepository.deleteAll();
        attributeRepository.deleteAll();
    }

    @Test
    @DisplayName("Full Dictionary Flow: Create attribute, values, aliases and enforce uniqueness constraints")
    void shouldExecuteFullDictionaryLifecycleWithConstraints() throws Exception {
        // 1. Create Attribute COLOR
        CreateAttributeRequest attrReq = CreateAttributeRequest.builder()
                .code("COLOR")
                .name("Màu sắc")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build();

        String attrRes = mockMvc.perform(post("/api/v1/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attrReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.code", is("COLOR")))
                .andExpect(jsonPath("$.data.name", is("Màu sắc")))
                .andReturn().getResponse().getContentAsString();

        Number attrIdNum = com.jayway.jsonpath.JsonPath.read(attrRes, "$.data.id");
        Long attrId = attrIdNum.longValue();

        // 2. Reject duplicate Attribute code
        mockMvc.perform(post("/api/v1/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attrReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")));

        // 3. Add Attribute Value RED
        CreateAttributeValueRequest valRedReq = CreateAttributeValueRequest.builder()
                .code("RED")
                .displayValue("Đỏ")
                .sortOrder(1)
                .build();

        String valRedRes = mockMvc.perform(post("/api/v1/attributes/" + attrId + "/values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valRedReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code", is("RED")))
                .andExpect(jsonPath("$.data.displayValue", is("Đỏ")))
                .andReturn().getResponse().getContentAsString();

        Number valRedIdNum = com.jayway.jsonpath.JsonPath.read(valRedRes, "$.data.id");
        Long valRedId = valRedIdNum.longValue();

        // 4. Reject duplicate value code for same attribute
        mockMvc.perform(post("/api/v1/attributes/" + attrId + "/values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valRedReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")));

        // 5. Add Attribute Alias "Màu sắc"
        CreateAttributeAliasRequest aliasReq = CreateAttributeAliasRequest.builder()
                .rawAttributeName("Màu sắc")
                .build();

        mockMvc.perform(post("/api/v1/attributes/" + attrId + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aliasReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rawAttributeName", is("Màu sắc")))
                .andExpect(jsonPath("$.data.normalizedName", is("màu sắc")));

        // 6. Reject duplicate attribute alias (even with different casing/whitespace)
        CreateAttributeAliasRequest duplicateAliasReq = CreateAttributeAliasRequest.builder()
                .rawAttributeName("  màu   SẮC  ")
                .build();

        mockMvc.perform(post("/api/v1/attributes/" + attrId + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateAliasReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")));

        // 7. Add Value Alias "Đỏ" to RED value
        CreateAttributeValueAliasRequest valAliasReq = CreateAttributeValueAliasRequest.builder()
                .rawValue("Đỏ")
                .build();

        mockMvc.perform(post("/api/v1/attributes/" + attrId + "/values/" + valRedId + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valAliasReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rawValue", is("Đỏ")))
                .andExpect(jsonPath("$.data.normalizedValue", is("đỏ")));

        // 8. Reject duplicate value alias within same attribute
        CreateAttributeValueAliasRequest dupValAliasReq = CreateAttributeValueAliasRequest.builder()
                .rawValue("  đỏ  ")
                .build();

        mockMvc.perform(post("/api/v1/attributes/" + attrId + "/values/" + valRedId + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupValAliasReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")));

        // 9. Composite Foreign Key check: reject when valId does not belong to attrId
        mockMvc.perform(post("/api/v1/attributes/" + 9999L + "/values/" + valRedId + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valAliasReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")));

        // 10. List attributes with nested values
        mockMvc.perform(get("/api/v1/attributes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code", is("COLOR")))
                .andExpect(jsonPath("$.data[0].values", hasSize(1)))
                .andExpect(jsonPath("$.data[0].values[0].code", is("RED")));
    }
}
