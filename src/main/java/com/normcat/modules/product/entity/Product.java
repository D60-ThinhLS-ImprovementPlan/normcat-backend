package com.normcat.modules.product.entity;

import com.normcat.common.entity.BaseAuditableEntity;
import com.normcat.modules.category.entity.Category;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(name = "products_sku_key", columnNames = {"sku"})
}, indexes = {
        @Index(name = "idx_products_category_status", columnList = "category_id, status"),
        @Index(name = "idx_products_price", columnList = "price")
})
public class Product extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", length = 50, nullable = false, unique = true)
    private String sku;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "price", precision = 14, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", length = 20, nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Type(JsonBinaryType.class)
    @Builder.Default
    @Column(name = "raw_attributes", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> rawAttributes = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "normalization_status", length = 30, nullable = false)
    private ProductNormalizationStatus normalizationStatus = ProductNormalizationStatus.PENDING;

    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();

    public void addAttributeValue(ProductAttributeValue pav) {
        attributeValues.add(pav);
        pav.setProduct(this);
    }

    public void removeAttributeValue(ProductAttributeValue pav) {
        attributeValues.remove(pav);
        pav.setProduct(null);
    }
}
