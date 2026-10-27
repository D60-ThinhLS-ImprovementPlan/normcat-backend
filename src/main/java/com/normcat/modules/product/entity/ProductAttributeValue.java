package com.normcat.modules.product.entity;

import com.normcat.common.entity.BaseCreationAuditableEntity;
import com.normcat.modules.dictionary.entity.AttributeValue;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_attribute_values", uniqueConstraints = {
        @UniqueConstraint(name = "uq_product_attribute_value", columnNames = {"product_id", "attribute_id", "attribute_value_id"})
}, indexes = {
        @Index(name = "idx_pav_search", columnList = "attribute_id, attribute_value_id, product_id"),
        @Index(name = "idx_pav_product", columnList = "product_id")
})
public class ProductAttributeValue extends BaseCreationAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;

    public void setAttributeValue(AttributeValue attributeValue) {
        this.attributeValue = attributeValue;
        if (attributeValue != null && attributeValue.getAttribute() != null) {
            this.attributeId = attributeValue.getAttribute().getId();
        }
    }
}
