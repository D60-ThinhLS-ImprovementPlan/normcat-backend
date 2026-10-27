package com.normcat.modules.product.service;

import com.normcat.common.exception.BusinessException;
import com.normcat.common.exception.ConflictException;
import com.normcat.common.exception.ErrorCode;
import com.normcat.common.exception.ResourceNotFoundException;
import com.normcat.modules.category.entity.Category;
import com.normcat.modules.category.repository.CategoryRepository;
import com.normcat.modules.dictionary.entity.Attribute;
import com.normcat.modules.dictionary.entity.AttributeDataType;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.dictionary.repository.AttributeValueRepository;
import com.normcat.modules.normalization.dto.response.NormalizedAttributeResult;
import com.normcat.modules.normalization.model.NormalizationStatus;
import com.normcat.modules.normalization.service.NormalizationEngine;
import com.normcat.modules.product.dto.request.CreateProductRequest;
import com.normcat.modules.product.dto.response.ProductResponse;
import com.normcat.modules.product.entity.Product;
import com.normcat.modules.product.entity.ProductAttributeValue;
import com.normcat.modules.product.entity.ProductNormalizationStatus;
import com.normcat.modules.product.entity.ProductStatus;
import com.normcat.modules.product.repository.ProductAttributeValueRepository;
import com.normcat.modules.product.repository.ProductRepository;
import com.normcat.modules.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductAttributeValueRepository productAttributeValueRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NormalizationEngine normalizationEngine;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Attribute ramAttr;
    private AttributeValue ram8GbValue;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Điện thoại")
                .slug("dien-thoai")
                .isActive(true)
                .build();

        ramAttr = Attribute.builder()
                .id(10L)
                .code("RAM")
                .name("Bộ nhớ RAM")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .isFilterable(true)
                .build();

        ram8GbValue = AttributeValue.builder()
                .id(100L)
                .attribute(ramAttr)
                .code("8GB")
                .displayValue("8 GB")
                .sortOrder(1)
                .build();
    }

    @Test
    @DisplayName("Should successfully ingest product with all attributes normalized")
    void createProduct_allNormalized_success() {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("PHONE-001")
                .name("Smartphone X")
                .categoryId(1L)
                .price(new BigDecimal("15000000.00"))
                .status(ProductStatus.ACTIVE)
                .rawAttributes(Map.of("RAM", "8G"))
                .build();

        NormalizedAttributeResult normResult = NormalizedAttributeResult.builder()
                .rawKey("RAM")
                .rawValue("8G")
                .status(NormalizationStatus.NORMALIZED)
                .attributeId(10L)
                .attributeCode("RAM")
                .attributeName("Bộ nhớ RAM")
                .dataType(AttributeDataType.STRING)
                .isMultiValued(false)
                .attributeValueId(100L)
                .attributeValueCode("8GB")
                .attributeDisplayValue("8 GB")
                .build();

        when(productRepository.existsBySku("PHONE-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(normalizationEngine.resolveAll(any())).thenReturn(List.of(normResult));
        when(attributeValueRepository.findById(100L)).thenReturn(Optional.of(ram8GbValue));

        Product savedProduct = Product.builder()
                .id(1L)
                .sku("PHONE-001")
                .name("Smartphone X")
                .category(category)
                .price(new BigDecimal("15000000.00"))
                .status(ProductStatus.ACTIVE)
                .rawAttributes(request.getRawAttributes())
                .normalizationStatus(ProductNormalizationStatus.NORMALIZED)
                .version(0L)
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("PHONE-001");
        assertThat(response.getNormalizationStatus()).isEqualTo(ProductNormalizationStatus.NORMALIZED);
        assertThat(response.getNormalizedAttributes()).hasSize(1);
        assertThat(response.getNormalizedAttributes().get(0).getAttributeCode()).isEqualTo("RAM");
        assertThat(response.getNormalizedAttributes().get(0).getAttributeValueCode()).isEqualTo("8GB");

        verify(productRepository).save(any(Product.class));
        verify(productAttributeValueRepository).saveAll(any());
    }

    @Test
    @DisplayName("Should ingest product as PARTIALLY_NORMALIZED when an attribute is unknown or partial")
    void createProduct_partiallyNormalized_success() {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("PHONE-002")
                .name("Smartphone Y")
                .categoryId(1L)
                .price(new BigDecimal("12000000.00"))
                .rawAttributes(Map.of("UnknownKey", "UnknownVal"))
                .build();

        NormalizedAttributeResult unmappedResult = NormalizedAttributeResult.builder()
                .rawKey("UnknownKey")
                .rawValue("UnknownVal")
                .status(NormalizationStatus.UNKNOWN)
                .build();

        when(productRepository.existsBySku("PHONE-002")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(normalizationEngine.resolveAll(any())).thenReturn(List.of(unmappedResult));

        Product savedProduct = Product.builder()
                .id(2L)
                .sku("PHONE-002")
                .name("Smartphone Y")
                .category(category)
                .price(new BigDecimal("12000000.00"))
                .status(ProductStatus.ACTIVE)
                .rawAttributes(request.getRawAttributes())
                .normalizationStatus(ProductNormalizationStatus.PARTIALLY_NORMALIZED)
                .version(0L)
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getNormalizationStatus()).isEqualTo(ProductNormalizationStatus.PARTIALLY_NORMALIZED);
        assertThat(response.getNormalizedAttributes()).isEmpty();
        verify(productAttributeValueRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when SKU already exists")
    void createProduct_duplicateSku_throwsConflict() {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("EXISTING-SKU")
                .name("Duplicate")
                .categoryId(1L)
                .price(BigDecimal.TEN)
                .build();

        when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category does not exist")
    void createProduct_categoryNotFound_throwsNotFound() {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("NEW-SKU")
                .name("No Category")
                .categoryId(999L)
                .price(BigDecimal.TEN)
                .build();

        when(productRepository.existsBySku("NEW-SKU")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("Should throw BusinessException when single-valued attribute receives multiple values")
    void createProduct_singleValuedViolation_throwsBusinessException() {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("PHONE-MULTI")
                .name("Multi RAM Phone")
                .categoryId(1L)
                .price(BigDecimal.TEN)
                .rawAttributes(Map.of("RAM1", "8GB", "RAM2", "16GB"))
                .build();

        NormalizedAttributeResult r1 = NormalizedAttributeResult.builder()
                .status(NormalizationStatus.NORMALIZED)
                .attributeId(10L)
                .attributeCode("RAM")
                .isMultiValued(false)
                .attributeValueId(100L)
                .build();

        NormalizedAttributeResult r2 = NormalizedAttributeResult.builder()
                .status(NormalizationStatus.NORMALIZED)
                .attributeId(10L)
                .attributeCode("RAM")
                .isMultiValued(false)
                .attributeValueId(101L)
                .build();

        when(productRepository.existsBySku("PHONE-MULTI")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(normalizationEngine.resolveAll(any())).thenReturn(List.of(r1, r2));

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SINGLE_VALUE_VIOLATION);
    }
}
