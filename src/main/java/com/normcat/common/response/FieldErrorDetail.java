package com.normcat.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDetail {
    private String field;
    private Object rejectedValue;
    private String message;
}
