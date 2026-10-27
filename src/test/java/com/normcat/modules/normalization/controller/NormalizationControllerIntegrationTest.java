package com.normcat.modules.normalization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.normcat.modules.dictionary.entity.Attribute;
import com.normcat.modules.dictionary.entity.AttributeAlias;
import com.normcat.modules.dictionary.entity.AttributeDataType;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.dictionary.entity.AttributeValueAlias;
import com.normcat.modules.dictionary.repository.AttributeAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeRepository;
import com.normcat.modules.dictionary.repository.AttributeValueAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
import com.normcat.modules.normalization.dto.request.NormalizationPreviewRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NormalizationControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("normcat_norm_test_db")
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
    void seedDictionary() {
        attributeValueAliasRepository.deleteAll();
        attributeAliasRepository.deleteAll();
        attributeValueRepository.deleteAll();
        attributeRepository.deleteAll();

        // Attribute COLOR
        Attribute color = attributeRepository.save(Attribute.builder()
                .code("COLOR")
                .name("Màu sắc")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build());

        AttributeValue red = attributeValueRepository.save(AttributeValue.builder()
                .attribute(color)
                .code("RED")
                .displayValue("Đỏ")
                .sortOrder(1)
                .build());

        attributeAliasRepository.save(AttributeAlias.builder()
                .attribute(color)
                .rawAttributeName("Colour")
                .normalizedName("colour")
                .build());

        attributeValueAliasRepository.save(AttributeValueAlias.builder()
                .attributeId(color.getId())
                .attributeValue(red)
                .rawValue("Red")
                .normalizedValue("red")
                .build());

        // Attribute STORAGE
        Attribute storage = attributeRepository.save(Attribute.builder()
                .code("STORAGE")
                .name("Dung lượng bộ nhớ")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build());

        AttributeValue rom256 = attributeValueRepository.save(AttributeValue.builder()
                .attribute(storage)
                .code("256GB")
                .displayValue("256 GB")
                .sortOrder(1)
                .build());

        attributeAliasRepository.save(AttributeAlias.builder()
                .attribute(storage)
                .rawAttributeName("Bộ nhớ trong")
                .normalizedName("bộ nhớ trong")
                .build());

        attributeValueAliasRepository.save(AttributeValueAlias.builder()
                .attributeId(storage.getId())
                .attributeValue(rom256)
                .rawValue("256G")
                .normalizedValue("256g")
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/normalization/preview - should normalize 100% deterministically")
    void shouldPreviewNormalizedSuccessfully() throws Exception {
        NormalizationPreviewRequest request = NormalizationPreviewRequest.builder()
                .key("Colour")
                .value("Red")
                .build();

        mockMvc.perform(post("/api/v1/normalization/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("NORMALIZED")))
                .andExpect(jsonPath("$.data.attributeCode", is("COLOR")))
                .andExpect(jsonPath("$.data.attributeValueCode", is("RED")))
                .andExpect(jsonPath("$.data.attributeDisplayValue", is("Đỏ")));
    }

    @Test
    @DisplayName("POST /api/v1/normalization/preview - should handle Vietnamese diacritics and return NORMALIZED")
    void shouldHandleVietnameseDiacritics() throws Exception {
        NormalizationPreviewRequest request = NormalizationPreviewRequest.builder()
                .key("   Bộ    nhớ    trong   ")
                .value("256G")
                .build();

        mockMvc.perform(post("/api/v1/normalization/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("NORMALIZED")))
                .andExpect(jsonPath("$.data.attributeCode", is("STORAGE")))
                .andExpect(jsonPath("$.data.attributeValueCode", is("256GB")))
                .andExpect(jsonPath("$.data.attributeDisplayValue", is("256 GB")));
    }

    @Test
    @DisplayName("POST /api/v1/normalization/preview - should return PARTIALLY_NORMALIZED for unmapped value")
    void shouldReturnPartiallyNormalizedForUnmappedValue() throws Exception {
        NormalizationPreviewRequest request = NormalizationPreviewRequest.builder()
                .key("Colour")
                .value("Đỏ đô")
                .build();

        mockMvc.perform(post("/api/v1/normalization/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PARTIALLY_NORMALIZED")))
                .andExpect(jsonPath("$.data.attributeCode", is("COLOR")))
                .andExpect(jsonPath("$.data.attributeValueId").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/normalization/preview - should return UNKNOWN for unknown key without crashing")
    void shouldReturnUnknownForUnmappedKey() throws Exception {
        NormalizationPreviewRequest request = NormalizationPreviewRequest.builder()
                .key("Chất liệu")
                .value("Cotton 100%")
                .build();

        mockMvc.perform(post("/api/v1/normalization/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("UNKNOWN")))
                .andExpect(jsonPath("$.data.attributeId").doesNotExist())
                .andExpect(jsonPath("$.data.attributeValueId").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/normalization/preview - should return 400 Bad Request on blank input")
    void shouldReturnBadRequestOnBlankInput() throws Exception {
        NormalizationPreviewRequest request = NormalizationPreviewRequest.builder()
                .key("")
                .value("")
                .build();

        mockMvc.perform(post("/api/v1/normalization/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
    }
}
