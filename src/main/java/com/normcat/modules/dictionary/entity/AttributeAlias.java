package com.normcat.modules.dictionary.entity;

import com.normcat.common.entity.BaseCreationAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attribute_aliases", uniqueConstraints = {
        @UniqueConstraint(name = "uq_attr_alias_normalized", columnNames = {"normalized_name"})
})
public class AttributeAlias extends BaseCreationAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(name = "raw_attribute_name", length = 100, nullable = false)
    private String rawAttributeName;

    @Column(name = "normalized_name", length = 100, nullable = false, unique = true)
    private String normalizedName;
}
