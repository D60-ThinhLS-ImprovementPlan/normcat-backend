package com.normcat.modules.dictionary.repository;

import com.normcat.modules.dictionary.entity.AttributeAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeAliasRepository extends JpaRepository<AttributeAlias, Long> {

    Optional<AttributeAlias> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);

    List<AttributeAlias> findByAttributeIdOrderByIdAsc(Long attributeId);
}
