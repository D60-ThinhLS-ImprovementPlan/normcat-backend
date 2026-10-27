package com.normcat.modules.normalization.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizationPreviewRequest {

    @NotBlank(message = "Attribute key cannot be blank")
    private String key;

    @NotBlank(message = "Attribute value cannot be blank")
    private String value;
}
