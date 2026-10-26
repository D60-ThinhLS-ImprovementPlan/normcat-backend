package com.normcat.modules.dictionary.entity;

import com.normcat.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attributes", uniqueConstraints = {
        @UniqueConstraint(name = "attributes_code_key", columnNames = {"code"})
})
public class Attribute extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "data_type", length = 20, nullable = false)
    private AttributeDataType dataType = AttributeDataType.STRING;

    @Builder.Default
    @Column(name = "is_multi_valued", nullable = false)
    private Boolean isMultiValued = false;

    @Builder.Default
    @Column(name = "is_filterable", nullable = false)
    private Boolean isFilterable = true;

    @Builder.Default
    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<AttributeValue> values = new ArrayList<>();

    public void addValue(AttributeValue value) {
        values.add(value);
        value.setAttribute(this);
    }

    public void removeValue(AttributeValue value) {
        values.remove(value);
        value.setAttribute(null);
    }
}
