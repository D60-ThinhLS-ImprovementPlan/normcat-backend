package com.normcat.modules.dictionary.repository;

import com.normcat.modules.dictionary.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {

    List<AttributeValue> findByAttributeIdOrderBySortOrderAscCodeAsc(Long attributeId);

    Optional<AttributeValue> findByAttributeIdAndCode(Long attributeId, String code);

    boolean existsByAttributeIdAndCode(Long attributeId, String code);

    Optional<AttributeValue> findByIdAndAttributeId(Long id, Long attributeId);
}
