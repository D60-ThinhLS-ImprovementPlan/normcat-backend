package com.normcat.modules.dictionary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueResponse {

    private Long id;
    private Long attributeId;
    private String code;
    private String displayValue;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
