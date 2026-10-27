package com.normcat.modules.product.dto.response;

import com.normcat.modules.product.entity.ProductNormalizationStatus;
import com.normcat.modules.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product details response with dual-storage audit and normalized attributes")
public class ProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Unique SKU", example = "IPHONE-15-PRO-128")
    private String sku;

    @Schema(description = "Product Name", example = "Điện thoại Apple iPhone 15 Pro 128GB")
    private String name;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Category Name", example = "Điện thoại di động")
    private String categoryName;

    @Schema(description = "Product Unit Price", example = "25990000.00")
    private BigDecimal price;

    @Schema(description = "Product Status", example = "ACTIVE")
    private ProductStatus status;

    @Schema(description = "Product Normalization Status", example = "NORMALIZED")
    private ProductNormalizationStatus normalizationStatus;

    @Schema(description = "Optimistic locking version", example = "0")
    private Long version;

    @Builder.Default
    @Schema(description = "Immutable snapshot of vendor raw attributes (JSONB)")
    private Map<String, Object> rawAttributes = new HashMap<>();

    @Builder.Default
    @Schema(description = "Standardized canonical attributes linked for faceted search")
    private List<NormalizedProductAttributeResponse> normalizedAttributes = new ArrayList<>();

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record last update timestamp")
    private Instant updatedAt;
}
