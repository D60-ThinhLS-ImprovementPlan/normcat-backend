package com.normcat.modules.product.dto.response;

import com.normcat.modules.dictionary.entity.AttributeDataType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Normalized attribute details linked to the product")
public class NormalizedProductAttributeResponse {

    @Schema(description = "Standard Attribute ID", example = "1")
    private Long attributeId;

    @Schema(description = "Standard Attribute Code", example = "COLOR")
    private String attributeCode;

    @Schema(description = "Standard Attribute Name", example = "Màu sắc")
    private String attributeName;

    @Schema(description = "Attribute Data Type", example = "STRING")
    private AttributeDataType dataType;

    @Schema(description = "Standard Attribute Value ID", example = "1")
    private Long attributeValueId;

    @Schema(description = "Standard Attribute Value Code", example = "RED")
    private String attributeValueCode;

    @Schema(description = "Standard Attribute Value Display Name", example = "Đỏ")
    private String attributeDisplayValue;
}
