package com.normcat.modules.dictionary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeValueAliasRequest {

    @NotBlank(message = "Raw value cannot be blank")
    @Size(min = 1, max = 100, message = "Raw value must be between 1 and 100 characters")
    private String rawValue;
}
