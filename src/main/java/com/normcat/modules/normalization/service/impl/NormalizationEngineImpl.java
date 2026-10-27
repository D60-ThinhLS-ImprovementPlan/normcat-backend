package com.normcat.modules.normalization.service.impl;

import com.normcat.common.util.StringNormalizer;
import com.normcat.modules.dictionary.entity.Attribute;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.dictionary.repository.AttributeAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeRepository;
import com.normcat.modules.dictionary.repository.AttributeValueAliasRepository;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
import com.normcat.modules.normalization.dto.response.NormalizedAttributeResult;
import com.normcat.modules.normalization.model.NormalizationStatus;
import com.normcat.modules.normalization.service.NormalizationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NormalizationEngineImpl implements NormalizationEngine {

    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final AttributeAliasRepository attributeAliasRepository;
    private final AttributeValueAliasRepository attributeValueAliasRepository;

    @Override
    @Transactional(readOnly = true)
    public NormalizedAttributeResult resolve(String rawKey, String rawValue) {
        log.debug("Resolving raw attribute: key='{}', value='{}'", rawKey, rawValue);

        String normalizedKey = StringNormalizer.normalize(rawKey);
        String normalizedValue = StringNormalizer.normalize(rawValue);

        if (normalizedKey == null || normalizedKey.isBlank()) {
            return NormalizedAttributeResult.builder()
                    .rawKey(rawKey)
                    .rawValue(rawValue)
                    .normalizedKey(normalizedKey)
                    .normalizedValue(normalizedValue)
                    .status(NormalizationStatus.UNKNOWN)
                    .build();
        }

        // =========================================================================
        // Phase 1 (Key Resolution): Find canonical Attribute
        // =========================================================================
        Attribute attribute = attributeAliasRepository.findByNormalizedName(normalizedKey)
                .map(alias -> alias.getAttribute())
                .orElseGet(() -> attributeRepository.findByCode(rawKey.trim().toUpperCase()).orElse(null));

        if (attribute == null) {
            log.debug("Phase 1 key resolution unmapped: '{}' -> UNKNOWN", rawKey);
            return NormalizedAttributeResult.builder()
                    .rawKey(rawKey)
                    .rawValue(rawValue)
                    .normalizedKey(normalizedKey)
                    .normalizedValue(normalizedValue)
                    .status(NormalizationStatus.UNKNOWN)
                    .build();
        }

        NormalizedAttributeResult.NormalizedAttributeResultBuilder resultBuilder = NormalizedAttributeResult.builder()
                .rawKey(rawKey)
                .rawValue(rawValue)
                .normalizedKey(normalizedKey)
                .normalizedValue(normalizedValue)
                .attributeId(attribute.getId())
                .attributeCode(attribute.getCode())
                .attributeName(attribute.getName())
                .dataType(attribute.getDataType())
                .isMultiValued(attribute.getIsMultiValued())
                .isFilterable(attribute.getIsFilterable());

        // =========================================================================
        // Phase 2 (Value Resolution): Find canonical AttributeValue for the Attribute
        // =========================================================================
        if (normalizedValue == null || normalizedValue.isBlank()) {
            log.debug("Phase 2 value resolution empty: '{}' -> PARTIALLY_NORMALIZED", rawValue);
            return resultBuilder.status(NormalizationStatus.PARTIALLY_NORMALIZED).build();
        }

        AttributeValue attributeValue = attributeValueAliasRepository
                .findByAttributeIdAndNormalizedValue(attribute.getId(), normalizedValue)
                .map(alias -> alias.getAttributeValue())
                .orElseGet(() -> attributeValueRepository.findByAttributeIdAndCode(attribute.getId(), rawValue.trim().toUpperCase()).orElse(null));

        if (attributeValue == null) {
            log.debug("Phase 2 value resolution unmapped for attribute '{}': '{}' -> PARTIALLY_NORMALIZED",
                    attribute.getCode(), rawValue);
            return resultBuilder.status(NormalizationStatus.PARTIALLY_NORMALIZED).build();
        }

        log.debug("Successfully normalized: key='{}' -> {}, value='{}' -> {}",
                rawKey, attribute.getCode(), rawValue, attributeValue.getCode());

        return resultBuilder
                .status(NormalizationStatus.NORMALIZED)
                .attributeValueId(attributeValue.getId())
                .attributeValueCode(attributeValue.getCode())
                .attributeDisplayValue(attributeValue.getDisplayValue())
                .sortOrder(attributeValue.getSortOrder())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NormalizedAttributeResult> resolveAll(Map<String, Object> rawAttributes) {
        if (rawAttributes == null || rawAttributes.isEmpty()) {
            return Collections.emptyList();
        }

        List<NormalizedAttributeResult> results = new ArrayList<>();
        for (Map.Entry<String, Object> entry : rawAttributes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Collection<?> collection) {
                for (Object item : collection) {
                    results.add(resolve(key, item != null ? item.toString() : null));
                }
            } else {
                results.add(resolve(key, value != null ? value.toString() : null));
            }
        }
        return results;
    }
}
