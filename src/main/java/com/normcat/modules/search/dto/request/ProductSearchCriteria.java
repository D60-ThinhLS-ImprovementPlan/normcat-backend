package com.normcat.modules.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Criteria parameters for product faceted search")
public class ProductSearchCriteria {

    @Schema(description = "Full-text search keyword matching product name or SKU", example = "ao thun")
    private String keyword;

    @Schema(description = "Unique category slug for filtering", example = "ao-so-mi-nam")
    private String categorySlug;

    @Schema(description = "Minimum price bound", example = "100000")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price bound", example = "500000")
    private BigDecimal maxPrice;

    @Builder.Default
    @Schema(description = "Dynamic attribute filters (attributeCode -> valueCode)", example = "{\"COLOR\": \"RED\", \"SIZE\": \"XL\"}")
    private Map<String, String> attrs = new HashMap<>();

    @Builder.Default
    @Schema(description = "Page index (0-based)", example = "0")
    private Integer page = 0;

    @Builder.Default
    @Schema(description = "Page size", example = "20")
    private Integer size = 20;

    @Builder.Default
    @Schema(description = "Sort expression: field,direction (e.g. price,asc or createdAt,desc)", example = "createdAt,desc")
    private String sort = "createdAt,desc";
}
