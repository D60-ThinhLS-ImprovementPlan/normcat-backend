package com.normcat.modules.normalization.service;

import com.normcat.modules.dictionary.entity.Attribute;
import com.normcat.modules.dictionary.entity.AttributeAlias;
import com.normcat.modules.dictionary.entity.AttributeDataType;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.dictionary.entity.AttributeValueAlias;
import com.normcat.modules.dictionary.repository.AttributeAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeRepository;
import com.normcat.modules.dictionary.repository.AttributeValueAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
import com.normcat.modules.normalization.dto.response.NormalizedAttributeResult;
import com.normcat.modules.normalization.model.NormalizationStatus;
import com.normcat.modules.normalization.service.impl.NormalizationEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NormalizationEngineTest {

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    @Mock
    private AttributeAliasRepository attributeAliasRepository;

    @Mock
    private AttributeValueAliasRepository attributeValueAliasRepository;

    @InjectMocks
    private NormalizationEngineImpl normalizationEngine;

    private Attribute colorAttr;
    private AttributeValue redValue;

    @BeforeEach
    void setUp() {
        colorAttr = Attribute.builder()
                .id(1L)
                .code("COLOR")
                .name("Màu sắc")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build();

        redValue = AttributeValue.builder()
                .id(10L)
                .attribute(colorAttr)
                .code("RED")
                .displayValue("Đỏ")
                .sortOrder(1)
                .build();
    }

    @Test
    @DisplayName("Should resolve 100% deterministically: Colour + Red -> COLOR: RED (NORMALIZED)")
    void shouldResolve100PercentDeterministically() {
        AttributeAlias keyAlias = AttributeAlias.builder()
                .id(100L)
                .attribute(colorAttr)
                .rawAttributeName("Colour")
                .normalizedName("colour")
                .build();

        AttributeValueAlias valAlias = AttributeValueAlias.builder()
                .id(200L)
                .attributeId(1L)
                .attributeValue(redValue)
                .rawValue("Red")
                .normalizedValue("red")
                .build();

        when(attributeAliasRepository.findByNormalizedName("colour")).thenReturn(Optional.of(keyAlias));
        when(attributeValueAliasRepository.findByAttributeIdAndNormalizedValue(1L, "red")).thenReturn(Optional.of(valAlias));

        NormalizedAttributeResult result = normalizationEngine.resolve("Colour", "Red");

        assertThat(result.getStatus()).isEqualTo(NormalizationStatus.NORMALIZED);
        assertThat(result.getAttributeCode()).isEqualTo("COLOR");
        assertThat(result.getAttributeValueCode()).isEqualTo("RED");
        assertThat(result.getAttributeDisplayValue()).isEqualTo("Đỏ");
    }

    @Test
    @DisplayName("Should flag PARTIALLY_NORMALIZED when attribute is resolved but raw value has no alias")
    void shouldFlagPartiallyNormalizedWhenValueUnmapped() {
        AttributeAlias keyAlias = AttributeAlias.builder()
                .id(100L)
                .attribute(colorAttr)
                .rawAttributeName("Màu")
                .normalizedName("màu")
                .build();

        when(attributeAliasRepository.findByNormalizedName("màu")).thenReturn(Optional.of(keyAlias));
        when(attributeValueAliasRepository.findByAttributeIdAndNormalizedValue(1L, "đỏ đô")).thenReturn(Optional.empty());
        when(attributeValueRepository.findByAttributeIdAndCode(1L, "ĐỎ ĐÔ")).thenReturn(Optional.empty());

        NormalizedAttributeResult result = normalizationEngine.resolve("Màu", "Đỏ đô");

        assertThat(result.getStatus()).isEqualTo(NormalizationStatus.PARTIALLY_NORMALIZED);
        assertThat(result.getAttributeCode()).isEqualTo("COLOR");
        assertThat(result.getAttributeValueId()).isNull();
        assertThat(result.getAttributeValueCode()).isNull();
    }

    @Test
    @DisplayName("Should flag UNKNOWN when attribute key is not in alias dictionary or catalog")
    void shouldFlagUnknownWhenAttributeKeyUnmapped() {
        when(attributeAliasRepository.findByNormalizedName("xuất xứ")).thenReturn(Optional.empty());
        when(attributeRepository.findByCode("XUẤT XỨ")).thenReturn(Optional.empty());

        NormalizedAttributeResult result = normalizationEngine.resolve("Xuất xứ", "Việt Nam");

        assertThat(result.getStatus()).isEqualTo(NormalizationStatus.UNKNOWN);
        assertThat(result.getAttributeId()).isNull();
        assertThat(result.getAttributeValueId()).isNull();
    }

    @Test
    @DisplayName("Should not crash on null or empty input, returns UNKNOWN safely")
    void shouldHandleNullOrEmptySafelyWithoutException() {
        NormalizedAttributeResult result1 = normalizationEngine.resolve(null, "something");
        assertThat(result1.getStatus()).isEqualTo(NormalizationStatus.UNKNOWN);

        NormalizedAttributeResult result2 = normalizationEngine.resolve("   ", "something");
        assertThat(result2.getStatus()).isEqualTo(NormalizationStatus.UNKNOWN);
    }
}
