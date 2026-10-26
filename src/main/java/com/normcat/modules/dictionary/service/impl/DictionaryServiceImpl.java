package com.normcat.modules.dictionary.service.impl;

import com.normcat.common.exception.ConflictException;
import com.normcat.common.exception.ResourceNotFoundException;
import com.normcat.common.util.StringNormalizer;
import com.normcat.modules.dictionary.dto.request.CreateAttributeAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueRequest;
import com.normcat.modules.dictionary.dto.response.AttributeAliasResponse;
import com.normcat.modules.dictionary.dto.response.AttributeResponse;
import com.normcat.modules.dictionary.dto.response.AttributeValueAliasResponse;
import com.normcat.modules.dictionary.dto.response.AttributeValueResponse;
import com.normcat.modules.dictionary.entity.*;
import com.normcat.modules.dictionary.repository.*;
import com.normcat.modules.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final AttributeAliasRepository attributeAliasRepository;
    private final AttributeValueAliasRepository attributeValueAliasRepository;

    @Override
    @Transactional
    public AttributeResponse createAttribute(CreateAttributeRequest request) {
        String code = request.getCode().trim().toUpperCase();
        log.info("Creating standard attribute with code: {}", code);

        if (attributeRepository.existsByCode(code)) {
            throw new ConflictException("Attribute with code '" + code + "' already exists");
        }

        Attribute attribute = Attribute.builder()
                .code(code)
                .name(request.getName().trim())
                .dataType(request.getDataType() != null ? request.getDataType() : AttributeDataType.STRING)
                .isMultiValued(request.getIsMultiValued() != null ? request.getIsMultiValued() : false)
                .isFilterable(request.getIsFilterable() != null ? request.getIsFilterable() : true)
                .build();

        Attribute saved = attributeRepository.save(attribute);
        log.info("Attribute created successfully with id: {}", saved.getId());
        return mapToAttributeResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeResponse getAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + id));
        return mapToAttributeResponse(attribute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeResponse> getAllAttributes() {
        log.debug("Fetching all standard attributes with nested values");
        return attributeRepository.findAllWithValues().stream()
                .map(this::mapToAttributeResponse)
                .toList();
    }

    @Override
    @Transactional
    public AttributeValueResponse addAttributeValue(Long attributeId, CreateAttributeValueRequest request) {
        String code = request.getCode().trim().toUpperCase();
        log.info("Adding value '{}' to attribute id {}", code, attributeId);

        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + attributeId));

        if (attributeValueRepository.existsByAttributeIdAndCode(attributeId, code)) {
            throw new ConflictException("Attribute value with code '" + code + "' already exists for attribute ID " + attributeId);
        }

        AttributeValue value = AttributeValue.builder()
                .attribute(attribute)
                .code(code)
                .displayValue(request.getDisplayValue().trim())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        AttributeValue saved = attributeValueRepository.save(value);
        log.info("Attribute value created successfully with id: {}", saved.getId());
        return mapToAttributeValueResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getAttributeValues(Long attributeId) {
        if (!attributeRepository.existsById(attributeId)) {
            throw new ResourceNotFoundException("Attribute not found with id: " + attributeId);
        }
        return attributeValueRepository.findByAttributeIdOrderBySortOrderAscCodeAsc(attributeId).stream()
                .map(this::mapToAttributeValueResponse)
                .toList();
    }

    @Override
    @Transactional
    public AttributeAliasResponse addAttributeAlias(Long attributeId, CreateAttributeAliasRequest request) {
        String rawName = request.getRawAttributeName().trim();
        String normalizedName = StringNormalizer.normalize(rawName);
        log.info("Adding alias '{}' (normalized: '{}') to attribute id {}", rawName, normalizedName, attributeId);

        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found with id: " + attributeId));

        if (attributeAliasRepository.existsByNormalizedName(normalizedName)) {
            throw new ConflictException("Attribute alias '" + rawName + "' already exists");
        }

        AttributeAlias alias = AttributeAlias.builder()
                .attribute(attribute)
                .rawAttributeName(rawName)
                .normalizedName(normalizedName)
                .build();

        AttributeAlias saved = attributeAliasRepository.save(alias);
        log.info("Attribute alias created successfully with id: {}", saved.getId());
        return mapToAttributeAliasResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeAliasResponse> getAttributeAliases(Long attributeId) {
        if (!attributeRepository.existsById(attributeId)) {
            throw new ResourceNotFoundException("Attribute not found with id: " + attributeId);
        }
        return attributeAliasRepository.findByAttributeIdOrderByIdAsc(attributeId).stream()
                .map(this::mapToAttributeAliasResponse)
                .toList();
    }

    @Override
    @Transactional
    public AttributeValueAliasResponse addAttributeValueAlias(Long attributeId, Long valueId, CreateAttributeValueAliasRequest request) {
        String rawVal = request.getRawValue().trim();
        String normalizedValue = StringNormalizer.normalize(rawVal);
        log.info("Adding value alias '{}' (normalized: '{}') to attribute id {} and value id {}",
                rawVal, normalizedValue, attributeId, valueId);

        if (!attributeRepository.existsById(attributeId)) {
            throw new ResourceNotFoundException("Attribute not found with id: " + attributeId);
        }

        AttributeValue value = attributeValueRepository.findByIdAndAttributeId(valueId, attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute value " + valueId + " not found for attribute " + attributeId));

        if (attributeValueAliasRepository.existsByAttributeIdAndNormalizedValue(attributeId, normalizedValue)) {
            throw new ConflictException("Value alias '" + rawVal + "' already exists for attribute ID " + attributeId);
        }

        AttributeValueAlias alias = AttributeValueAlias.builder()
                .attributeId(attributeId)
                .attributeValue(value)
                .rawValue(rawVal)
                .normalizedValue(normalizedValue)
                .build();

        AttributeValueAlias saved = attributeValueAliasRepository.save(alias);
        log.info("Attribute value alias created successfully with id: {}", saved.getId());
        return mapToAttributeValueAliasResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueAliasResponse> getAttributeValueAliases(Long attributeId, Long valueId) {
        if (!attributeRepository.existsById(attributeId)) {
            throw new ResourceNotFoundException("Attribute not found with id: " + attributeId);
        }
        if (!attributeValueRepository.existsById(valueId)) {
            throw new ResourceNotFoundException("Attribute value not found with id: " + valueId);
        }
        return attributeValueAliasRepository.findByAttributeIdAndAttributeValueIdOrderByIdAsc(attributeId, valueId).stream()
                .map(this::mapToAttributeValueAliasResponse)
                .toList();
    }

    private AttributeResponse mapToAttributeResponse(Attribute attribute) {
        List<AttributeValueResponse> valueResponses = attribute.getValues() == null ? List.of() :
                attribute.getValues().stream()
                        .map(this::mapToAttributeValueResponse)
                        .toList();

        return AttributeResponse.builder()
                .id(attribute.getId())
                .code(attribute.getCode())
                .name(attribute.getName())
                .dataType(attribute.getDataType())
                .isMultiValued(attribute.getIsMultiValued())
                .isFilterable(attribute.getIsFilterable())
                .values(valueResponses)
                .createdAt(attribute.getCreatedAt())
                .updatedAt(attribute.getUpdatedAt())
                .build();
    }

    private AttributeValueResponse mapToAttributeValueResponse(AttributeValue value) {
        return AttributeValueResponse.builder()
                .id(value.getId())
                .attributeId(value.getAttribute() != null ? value.getAttribute().getId() : null)
                .code(value.getCode())
                .displayValue(value.getDisplayValue())
                .sortOrder(value.getSortOrder())
                .createdAt(value.getCreatedAt())
                .updatedAt(value.getUpdatedAt())
                .build();
    }

    private AttributeAliasResponse mapToAttributeAliasResponse(AttributeAlias alias) {
        return AttributeAliasResponse.builder()
                .id(alias.getId())
                .attributeId(alias.getAttribute() != null ? alias.getAttribute().getId() : null)
                .rawAttributeName(alias.getRawAttributeName())
                .normalizedName(alias.getNormalizedName())
                .createdAt(alias.getCreatedAt())
                .build();
    }

    private AttributeValueAliasResponse mapToAttributeValueAliasResponse(AttributeValueAlias alias) {
        return AttributeValueAliasResponse.builder()
                .id(alias.getId())
                .attributeId(alias.getAttributeId())
                .attributeValueId(alias.getAttributeValue() != null ? alias.getAttributeValue().getId() : null)
                .rawValue(alias.getRawValue())
                .normalizedValue(alias.getNormalizedValue())
                .createdAt(alias.getCreatedAt())
                .build();
    }
}
