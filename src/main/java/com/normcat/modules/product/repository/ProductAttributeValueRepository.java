package com.normcat.modules.product.repository;

import com.normcat.modules.product.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {

    List<ProductAttributeValue> findByProductId(Long productId);

    void deleteByProductId(Long productId);

    @Query("SELECT pav FROM ProductAttributeValue pav " +
           "JOIN FETCH pav.attributeValue av " +
           "JOIN FETCH av.attribute a " +
           "WHERE pav.product.id IN :productIds")
    List<ProductAttributeValue> findAllWithDetailsByProductIdIn(@Param("productIds") Collection<Long> productIds);
}
