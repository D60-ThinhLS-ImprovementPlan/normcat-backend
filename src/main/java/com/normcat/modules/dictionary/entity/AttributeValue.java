package com.normcat.modules.dictionary.entity;

import com.normcat.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attribute_values", uniqueConstraints = {
        @UniqueConstraint(name = "uq_attribute_value_code", columnNames = {"attribute_id", "code"}),
        @UniqueConstraint(name = "uq_attribute_value_composite", columnNames = {"id", "attribute_id"})
})
public class AttributeValue extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "display_value", length = 100, nullable = false)
    private String displayValue;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
