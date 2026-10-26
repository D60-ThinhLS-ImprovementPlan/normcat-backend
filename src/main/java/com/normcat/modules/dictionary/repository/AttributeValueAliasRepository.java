package com.normcat.modules.dictionary.repository;

import com.normcat.modules.dictionary.entity.AttributeValueAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeValueAliasRepository extends JpaRepository<AttributeValueAlias, Long> {

    Optional<AttributeValueAlias> findByAttributeIdAndNormalizedValue(Long attributeId, String normalizedValue);

    boolean existsByAttributeIdAndNormalizedValue(Long attributeId, String normalizedValue);

    List<AttributeValueAlias> findByAttributeIdAndAttributeValueIdOrderByIdAsc(Long attributeId, Long attributeValueId);

    List<AttributeValueAlias> findByAttributeIdOrderByIdAsc(Long attributeId);
}
