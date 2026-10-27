package com.normcat.modules.product.dto.request;

import com.normcat.modules.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for ingesting a new product")
public class CreateProductRequest {

    @NotBlank(message = "SKU must not be blank")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Schema(description = "Unique Stock Keeping Unit (SKU)", example = "IPHONE-15-PRO-128")
    private String sku;

    @NotBlank(message = "Product name must not be blank")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Schema(description = "Product display name", example = "Điện thoại Apple iPhone 15 Pro 128GB")
    private String name;

    @NotNull(message = "Category ID must not be null")
    @Schema(description = "Primary category ID", example = "1")
    private Long categoryId;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @Schema(description = "Product unit price", example = "25990000.00")
    private BigDecimal price;

    @Builder.Default
    @Schema(description = "Product lifecycle status", example = "ACTIVE")
    private ProductStatus status = ProductStatus.ACTIVE;

    @Builder.Default
    @Schema(description = "Raw multi-vendor attribute key-value map", example = "{\"Màu sắc\": \"Đỏ\", \"RAM\": \"8G\", \"Xuất xứ\": \"Việt Nam\"}")
    private Map<String, Object> rawAttributes = new HashMap<>();
}
