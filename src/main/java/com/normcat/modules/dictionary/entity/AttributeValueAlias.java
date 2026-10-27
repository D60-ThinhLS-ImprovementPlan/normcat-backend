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
@Table(name = "attribute_value_aliases", uniqueConstraints = {
        @UniqueConstraint(name = "uq_val_alias_normalized", columnNames = {"attribute_id", "normalized_value"})
})
public class AttributeValueAlias extends BaseCreationAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;

    @Column(name = "raw_value", length = 100, nullable = false)
    private String rawValue;

    @Column(name = "normalized_value", length = 100, nullable = false)
    private String normalizedValue;

    public void setAttributeValue(AttributeValue attributeValue) {
        this.attributeValue = attributeValue;
        if (attributeValue != null && attributeValue.getAttribute() != null) {
            this.attributeId = attributeValue.getAttribute().getId();
        }
    }
}
