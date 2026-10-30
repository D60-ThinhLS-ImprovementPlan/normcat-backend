package com.normcat.modules.search.controller;

import com.normcat.common.response.ApiResponse;
import com.normcat.common.response.PageResponse;
import com.normcat.modules.search.dto.request.ProductSearchCriteria;
import com.normcat.modules.search.dto.response.ProductSearchResponse;
import com.normcat.modules.search.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Product Search", description = "Customer-facing faceted product search API with dynamic attribute filtering and pagination")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping("/products")
    @Operation(
            summary = "Search and filter products",
            description = "Searches products by keyword (name/SKU), category slug, price range, dynamic attribute facets (e.g. attrs[COLOR]=RED&attrs[SIZE]=XL), with pagination and sorting."
    )
    public ResponseEntity<ApiResponse<PageResponse<ProductSearchResponse>>> searchProducts(
            @Parameter(description = "Keyword to search in product name or SKU")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Category slug to filter by")
            @RequestParam(required = false) String categorySlug,

            @Parameter(description = "Minimum price bound")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum price bound")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort property and direction (e.g., price,asc or createdAt,desc)")
            @RequestParam(defaultValue = "createdAt,desc") String sort,

            @Parameter(hidden = true)
            @RequestParam Map<String, String> allParams
    ) {
        Map<String, String> attrs = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("attrs[") && key.endsWith("]")) {
                String attrCode = key.substring(6, key.length() - 1).trim();
                if (!attrCode.isEmpty() && entry.getValue() != null && !entry.getValue().isBlank()) {
                    attrs.put(attrCode, entry.getValue().trim());
                }
            }
        }

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categorySlug(categorySlug)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .attrs(attrs)
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        PageResponse<ProductSearchResponse> response = productSearchService.searchProducts(criteria);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
