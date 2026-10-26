package com.normcat.modules.category.service;

import com.normcat.modules.category.dto.request.CreateCategoryRequest;
import com.normcat.modules.category.dto.response.CategoryNodeResponse;
import com.normcat.modules.category.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse getCategoryById(Long id);

    List<CategoryNodeResponse> getCategoryTree();
}
