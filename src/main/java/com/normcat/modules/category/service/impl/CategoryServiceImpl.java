package com.normcat.modules.category.service.impl;

import com.normcat.common.exception.BusinessException;
import com.normcat.common.exception.ConflictException;
import com.normcat.common.exception.ErrorCode;
import com.normcat.common.exception.ResourceNotFoundException;
import com.normcat.modules.category.dto.request.CreateCategoryRequest;
import com.normcat.modules.category.dto.response.CategoryNodeResponse;
import com.normcat.modules.category.dto.response.CategoryResponse;
import com.normcat.modules.category.entity.Category;
import com.normcat.modules.category.repository.CategoryRepository;
import com.normcat.modules.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category with slug: {}", request.getSlug());

        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Category with slug '" + request.getSlug() + "' already exists");
        }

        if (request.getParentId() != null) {
            categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(request.getSlug().trim().toLowerCase())
                .parentId(request.getParentId())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryNodeResponse> getCategoryTree() {
        log.debug("Fetching active categories to build hierarchical tree");
        List<Category> allCategories = categoryRepository.findByIsActiveTrue();

        Map<Long, CategoryNodeResponse> nodeMap = new LinkedHashMap<>();
        for (Category category : allCategories) {
            nodeMap.put(category.getId(), CategoryNodeResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .slug(category.getSlug())
                    .description(category.getDescription())
                    .isActive(category.getIsActive())
                    .children(new ArrayList<>())
                    .build());
        }

        List<CategoryNodeResponse> rootNodes = new ArrayList<>();
        for (Category category : allCategories) {
            CategoryNodeResponse currentNode = nodeMap.get(category.getId());
            Long parentId = category.getParentId();
            if (parentId == null) {
                rootNodes.add(currentNode);
            } else {
                CategoryNodeResponse parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    parentNode.getChildren().add(currentNode);
                } else {
                    rootNodes.add(currentNode);
                }
            }
        }

        return rootNodes;
    }

    /**
     * Validates that assigning targetParentId to categoryId will not produce a cycle.
     */
    public void validateNoCycle(Long categoryId, Long targetParentId) {
        if (targetParentId == null || categoryId == null) {
            return;
        }

        if (targetParentId.equals(categoryId)) {
            throw new BusinessException(ErrorCode.CYCLIC_CATEGORY_HIERARCHY,
                    "Category cannot be assigned as its own parent");
        }

        Long currentParentId = targetParentId;
        Set<Long> visited = new HashSet<>();
        while (currentParentId != null) {
            if (currentParentId.equals(categoryId)) {
                throw new BusinessException(ErrorCode.CYCLIC_CATEGORY_HIERARCHY,
                        "Category cannot be assigned to its own descendant");
            }
            if (!visited.add(currentParentId)) {
                throw new BusinessException(ErrorCode.CYCLIC_CATEGORY_HIERARCHY,
                        "Cycle detected in category hierarchy");
            }
            currentParentId = categoryRepository.findById(currentParentId)
                    .map(Category::getParentId)
                    .orElse(null);
        }
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParentId())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
