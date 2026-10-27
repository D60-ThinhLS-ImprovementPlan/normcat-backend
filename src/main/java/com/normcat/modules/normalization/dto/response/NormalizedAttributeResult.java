package com.normcat.modules.normalization.dto.response;

import com.normcat.modules.dictionary.entity.AttributeDataType;
import com.normcat.modules.normalization.model.NormalizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedAttributeResult {

    private String rawKey;
    private String rawValue;
    private String normalizedKey;
    private String normalizedValue;
    private NormalizationStatus status;

    // Resolved Attribute Details
    private Long attributeId;
    private String attributeCode;
    private String attributeName;
    private AttributeDataType dataType;
    private Boolean isMultiValued;
    private Boolean isFilterable;

    // Resolved Attribute Value Details
    private Long attributeValueId;
    private String attributeValueCode;
    private String attributeDisplayValue;
    private Integer sortOrder;
}
