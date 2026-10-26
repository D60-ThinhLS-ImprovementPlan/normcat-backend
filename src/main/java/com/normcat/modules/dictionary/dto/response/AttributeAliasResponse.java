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
public class AttributeAliasResponse {

    private Long id;
    private Long attributeId;
    private String rawAttributeName;
    private String normalizedName;
    private Instant createdAt;
}
