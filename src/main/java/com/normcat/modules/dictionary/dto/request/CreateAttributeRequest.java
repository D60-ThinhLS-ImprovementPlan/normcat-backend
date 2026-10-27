package com.normcat.modules.dictionary.dto.request;

import com.normcat.modules.dictionary.entity.AttributeDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeRequest {

    @NotBlank(message = "Attribute code cannot be blank")
    @Size(min = 2, max = 50, message = "Attribute code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Attribute code must contain only uppercase alphanumeric characters and underscores")
    private String code;

    @NotBlank(message = "Attribute name cannot be blank")
    @Size(min = 2, max = 100, message = "Attribute name must be between 2 and 100 characters")
    private String name;

    @Builder.Default
    private AttributeDataType dataType = AttributeDataType.STRING;

    @Builder.Default
    private Boolean isMultiValued = false;

    @Builder.Default
    private Boolean isFilterable = true;
}
