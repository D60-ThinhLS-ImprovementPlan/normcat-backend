package com.normcat.modules.dictionary.dto.response;

import com.normcat.modules.dictionary.entity.AttributeDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeResponse {

    private Long id;
    private String code;
    private String name;
    private AttributeDataType dataType;
    private Boolean isMultiValued;
    private Boolean isFilterable;
    @Builder.Default
    private List<AttributeValueResponse> values = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
