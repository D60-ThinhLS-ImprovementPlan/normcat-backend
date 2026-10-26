package com.normcat.modules.category.service;

import com.normcat.common.exception.BusinessException;
import com.normcat.common.exception.ConflictException;
import com.normcat.common.exception.ErrorCode;
import com.normcat.common.exception.ResourceNotFoundException;
import com.normcat.modules.category.dto.request.CreateCategoryRequest;
import com.normcat.modules.category.dto.response.CategoryNodeResponse;
import com.normcat.modules.category.dto.response.CategoryResponse;
import com.normcat.modules.category.entity.Category;
import com.normcat.modules.category.repository.CategoryRepository;
import com.normcat.modules.category.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
                .id(1L)
                .name("Thời trang nam")
                .slug("thoi-trang-nam")
                .description("Men fashion")
                .isActive(true)
                .build();
        rootCategory.setCreatedAt(Instant.now());
        rootCategory.setUpdatedAt(Instant.now());

        childCategory = Category.builder()
                .id(2L)
                .name("Áo sơ mi")
                .slug("ao-so-mi")
                .parentId(1L)
                .description("Shirts")
                .isActive(true)
                .build();
        childCategory.setCreatedAt(Instant.now());
        childCategory.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should create root category successfully when valid request provided")
    void shouldCreateRootCategorySuccessfully() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Thời trang nam")
                .slug("thoi-trang-nam")
                .description("Men fashion")
                .build();

        when(categoryRepository.existsBySlug("thoi-trang-nam")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(rootCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSlug()).isEqualTo("thoi-trang-nam");
        assertThat(response.getParentId()).isNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should create child category successfully when parent exists")
    void shouldCreateChildCategorySuccessfully() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Áo sơ mi")
                .slug("ao-so-mi")
                .parentId(1L)
                .description("Shirts")
                .build();

        when(categoryRepository.existsBySlug("ao-so-mi")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(childCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getParentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ConflictException when category slug already exists")
    void shouldThrowConflictExceptionWhenSlugExists() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Thời trang nam")
                .slug("thoi-trang-nam")
                .build();

        when(categoryRepository.existsBySlug("thoi-trang-nam")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when parentId does not exist")
    void shouldThrowResourceNotFoundExceptionWhenParentNotFound() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Áo sơ mi")
                .slug("ao-so-mi")
                .parentId(999L)
                .build();

        when(categoryRepository.existsBySlug("ao-so-mi")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parent category not found");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when category is assigned as its own parent")
    void shouldThrowBusinessExceptionWhenSelfParentCycleDetected() {
        assertThatThrownBy(() -> categoryService.validateNoCycle(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.CYCLIC_CATEGORY_HIERARCHY));
    }

    @Test
    @DisplayName("Should throw BusinessException when assigning parent produces descendant cycle")
    void shouldThrowBusinessExceptionWhenDescendantParentCycleDetected() {
        // Node 1 (Root) -> Node 2 (Child) -> Node 3 (Grandchild)
        // Attempting to set Node 1's parent to Node 3 creates a cycle (1 -> 3 -> 2 -> 1)
        Category node2 = Category.builder().id(2L).parentId(1L).build();
        Category node3 = Category.builder().id(3L).parentId(2L).build();

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(node3));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(node2));

        assertThatThrownBy(() -> categoryService.validateNoCycle(1L, 3L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.CYCLIC_CATEGORY_HIERARCHY));
    }

    @Test
    @DisplayName("Should build hierarchical category tree with nested children")
    void shouldBuildHierarchicalCategoryTreeCorrectly() {
        Category subChild = Category.builder()
                .id(3L)
                .name("Áo sơ mi Oxford")
                .slug("ao-so-mi-oxford")
                .parentId(2L)
                .isActive(true)
                .build();

        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(rootCategory, childCategory, subChild));

        List<CategoryNodeResponse> tree = categoryService.getCategoryTree();

        assertThat(tree).hasSize(1);
        CategoryNodeResponse root = tree.get(0);
        assertThat(root.getId()).isEqualTo(1L);
        assertThat(root.getChildren()).hasSize(1);

        CategoryNodeResponse child = root.getChildren().get(0);
        assertThat(child.getId()).isEqualTo(2L);
        assertThat(child.getChildren()).hasSize(1);

        CategoryNodeResponse leaf = child.getChildren().get(0);
        assertThat(leaf.getId()).isEqualTo(3L);
        assertThat(leaf.getChildren()).isEmpty();
    }
}
