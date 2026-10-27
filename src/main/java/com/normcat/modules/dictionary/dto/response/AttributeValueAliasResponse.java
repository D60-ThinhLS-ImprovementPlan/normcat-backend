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
public class AttributeValueAliasResponse {

    private Long id;
    private Long attributeId;
    private Long attributeValueId;
    private String rawValue;
    private String normalizedValue;
    private Instant createdAt;
}
