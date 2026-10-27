package com.normcat.modules.product.repository;

import com.normcat.modules.product.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {

    List<ProductAttributeValue> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
