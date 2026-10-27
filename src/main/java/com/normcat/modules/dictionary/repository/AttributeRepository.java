package com.normcat.modules.dictionary.repository;

import com.normcat.modules.dictionary.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {

    Optional<Attribute> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT DISTINCT a FROM Attribute a LEFT JOIN FETCH a.values ORDER BY a.id ASC")
    List<Attribute> findAllWithValues();
}
