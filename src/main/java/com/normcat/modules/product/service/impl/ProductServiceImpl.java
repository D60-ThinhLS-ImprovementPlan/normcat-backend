package com.normcat.modules.product.service.impl;

import com.normcat.common.exception.BusinessException;
import com.normcat.common.exception.ConflictException;
import com.normcat.common.exception.ErrorCode;
import com.normcat.common.exception.ResourceNotFoundException;
import com.normcat.modules.category.entity.Category;
import com.normcat.modules.category.repository.CategoryRepository;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
import com.normcat.modules.normalization.dto.response.NormalizedAttributeResult;
import com.normcat.modules.normalization.model.NormalizationStatus;
import com.normcat.modules.normalization.service.NormalizationEngine;
import com.normcat.modules.product.dto.request.CreateProductRequest;
import com.normcat.modules.product.dto.response.NormalizedProductAttributeResponse;
import com.normcat.modules.product.dto.response.ProductResponse;
import com.normcat.modules.product.entity.Product;
import com.normcat.modules.product.entity.ProductAttributeValue;
import com.normcat.modules.product.entity.ProductNormalizationStatus;
import com.normcat.modules.product.entity.ProductStatus;
import com.normcat.modules.product.repository.ProductAttributeValueRepository;
import com.normcat.modules.product.repository.ProductRepository;
import com.normcat.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final CategoryRepository categoryRepository;
    private final NormalizationEngine normalizationEngine;
    private final AttributeValueRepository attributeValueRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Ingesting product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new ConflictException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Map<String, Object> rawAttributes = request.getRawAttributes() != null ? request.getRawAttributes() : Collections.emptyMap();
        List<NormalizedAttributeResult> normalizedResults = normalizationEngine.resolveAll(rawAttributes);

        // Enforce single-valued attribute invariant
        validateSingleValuedAttributes(normalizedResults);

        // Determine aggregate product normalization status
        ProductNormalizationStatus productNormStatus;
        if (rawAttributes.isEmpty()) {
            productNormStatus = ProductNormalizationStatus.NORMALIZED;
        } else {
            boolean allNormalized = normalizedResults.stream()
                    .allMatch(r -> r.getStatus() == NormalizationStatus.NORMALIZED);
            productNormStatus = allNormalized ? ProductNormalizationStatus.NORMALIZED : ProductNormalizationStatus.PARTIALLY_NORMALIZED;
        }

        Product product = Product.builder()
                .sku(request.getSku().trim())
                .name(request.getName().trim())
                .category(category)
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE)
                .rawAttributes(rawAttributes)
                .normalizationStatus(productNormStatus)
                .version(0L)
                .build();

        Product savedProduct = productRepository.save(product);

        // Persist standardized attribute links for faceted search
        List<ProductAttributeValue> pavList = new ArrayList<>();
        for (NormalizedAttributeResult result : normalizedResults) {
            if (result.getStatus() == NormalizationStatus.NORMALIZED && result.getAttributeValueId() != null) {
                AttributeValue attributeValue = attributeValueRepository.findById(result.getAttributeValueId())
                        .orElseThrow(() -> new ResourceNotFoundException("AttributeValue not found with id: " + result.getAttributeValueId()));

                ProductAttributeValue pav = ProductAttributeValue.builder()
                        .product(savedProduct)
                        .attributeId(result.getAttributeId())
                        .attributeValue(attributeValue)
                        .build();
                pavList.add(pav);
            }
        }

        if (!pavList.isEmpty()) {
            productAttributeValueRepository.saveAll(pavList);
            savedProduct.setAttributeValues(pavList);
        }

        log.info("Product ingested successfully with id: {}, normalization_status: {}", savedProduct.getId(), productNormStatus);
        return mapToProductResponse(savedProduct, pavList);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        List<ProductAttributeValue> pavList = productAttributeValueRepository.findByProductId(id);
        return mapToProductResponse(product, pavList);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with sku: " + sku));
        List<ProductAttributeValue> pavList = productAttributeValueRepository.findByProductId(product.getId());
        return mapToProductResponse(product, pavList);
    }

    private void validateSingleValuedAttributes(List<NormalizedAttributeResult> normalizedResults) {
        Map<Long, List<NormalizedAttributeResult>> grouped = normalizedResults.stream()
                .filter(r -> r.getStatus() == NormalizationStatus.NORMALIZED && r.getAttributeId() != null)
                .collect(Collectors.groupingBy(NormalizedAttributeResult::getAttributeId));

        for (Map.Entry<Long, List<NormalizedAttributeResult>> entry : grouped.entrySet()) {
            List<NormalizedAttributeResult> values = entry.getValue();
            if (!values.isEmpty()) {
                Boolean isMulti = values.get(0).getIsMultiValued();
                if (Boolean.FALSE.equals(isMulti) && values.size() > 1) {
                    throw new BusinessException(ErrorCode.SINGLE_VALUE_VIOLATION,
                            "Single-valued attribute '" + values.get(0).getAttributeCode() + "' cannot have multiple values on product");
                }
            }
        }
    }

    private ProductResponse mapToProductResponse(Product product, List<ProductAttributeValue> pavList) {
        List<NormalizedProductAttributeResponse> normAttrResponses = pavList.stream()
                .map(pav -> {
                    AttributeValue val = pav.getAttributeValue();
                    return NormalizedProductAttributeResponse.builder()
                            .attributeId(pav.getAttributeId())
                            .attributeCode(val.getAttribute() != null ? val.getAttribute().getCode() : null)
                            .attributeName(val.getAttribute() != null ? val.getAttribute().getName() : null)
                            .dataType(val.getAttribute() != null ? val.getAttribute().getDataType() : null)
                            .attributeValueId(val.getId())
                            .attributeValueCode(val.getCode())
                            .attributeDisplayValue(val.getDisplayValue())
                            .build();
                })
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .price(product.getPrice())
                .status(product.getStatus())
                .normalizationStatus(product.getNormalizationStatus())
                .version(product.getVersion())
                .rawAttributes(product.getRawAttributes() != null ? product.getRawAttributes() : Collections.emptyMap())
                .normalizedAttributes(normAttrResponses)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
