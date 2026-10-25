package com.normcat.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // General & Validation Errors
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request payload or parameters"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields"),

    // Resource Errors
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Requested resource not found"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Resource with specified identifier already exists"),

    // Catalog & Normalization Errors
    ALIAS_CONFLICT(HttpStatus.CONFLICT, "Attribute or value alias conflicts with an existing canonical mapping"),
    SINGLE_VALUE_VIOLATION(HttpStatus.BAD_REQUEST, "Single-valued attribute cannot hold multiple values on a product"),
    INVALID_ATTRIBUTE_TYPE(HttpStatus.BAD_REQUEST, "Attribute value does not conform to required data type"),
    CYCLIC_CATEGORY_HIERARCHY(HttpStatus.BAD_REQUEST, "Category cannot be assigned as its own ancestor");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
