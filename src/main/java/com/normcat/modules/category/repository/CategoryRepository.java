package com.normcat.modules.category.repository;

import com.normcat.modules.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByIsActiveTrue();

    List<Category> findByParentId(Long parentId);

    List<Category> findByParentIdIsNull();
}
