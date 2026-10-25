package com.normcat.modules.category.controller;

import com.normcat.common.response.ApiResponse;
import com.normcat.modules.category.dto.request.CreateCategoryRequest;
import com.normcat.modules.category.dto.response.CategoryNodeResponse;
import com.normcat.modules.category.dto.response.CategoryResponse;
import com.normcat.modules.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Endpoints for managing categories and hierarchical catalog tree")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a category", description = "Creates a root or child category with unique slug validation")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Category created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves category details by its unique identifier")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get hierarchical category tree", description = "Retrieves all active categories organized in a recursive parent-child tree structure")
    public ResponseEntity<ApiResponse<List<CategoryNodeResponse>>> getCategoryTree() {
        List<CategoryNodeResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.ok(tree));
    }
}
