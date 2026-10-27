package com.normcat.modules.dictionary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeValueRequest {

    @NotBlank(message = "Attribute value code cannot be blank")
    @Size(min = 1, max = 50, message = "Attribute value code must be between 1 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Attribute value code must contain only uppercase alphanumeric characters and underscores")
    private String code;

    @NotBlank(message = "Display value cannot be blank")
    @Size(min = 1, max = 100, message = "Display value must be between 1 and 100 characters")
    private String displayValue;

    @Builder.Default
    @PositiveOrZero(message = "Sort order must be greater than or equal to 0")
    private Integer sortOrder = 0;
}
