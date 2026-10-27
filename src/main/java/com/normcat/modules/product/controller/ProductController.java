package com.normcat.modules.product.controller;

import com.normcat.common.response.ApiResponse;
import com.normcat.modules.product.dto.request.CreateProductRequest;
import com.normcat.modules.product.dto.response.ProductResponse;
import com.normcat.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for ingesting and retrieving products with dual-storage audit")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Ingest a new product", description = "Ingests a vendor product payload with raw attributes, invokes the 2-phase NormalizationEngine, and dual-stores raw JSONB snapshot and canonical relational mappings atomically.")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product ingested successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves product details including raw JSONB attributes snapshot and canonical normalized attributes.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves product details by its unique SKU identifier.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
