package com.normcat.modules.dictionary.controller;

import com.normcat.common.response.ApiResponse;
import com.normcat.modules.dictionary.dto.request.CreateAttributeAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueRequest;
import com.normcat.modules.dictionary.dto.response.AttributeAliasResponse;
import com.normcat.modules.dictionary.dto.response.AttributeResponse;
import com.normcat.modules.dictionary.dto.response.AttributeValueAliasResponse;
import com.normcat.modules.dictionary.dto.response.AttributeValueResponse;
import com.normcat.modules.dictionary.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/attributes", "/api/v1/dictionary/attributes"})
@RequiredArgsConstructor
@Tag(name = "Dictionary Management", description = "Endpoints for managing standard catalog attributes, values, and alias mappings")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @PostMapping
    @Operation(summary = "Create standard attribute", description = "Defines a new canonical attribute (e.g. COLOR, SIZE)")
    public ResponseEntity<ApiResponse<AttributeResponse>> createAttribute(@Valid @RequestBody CreateAttributeRequest request) {
        AttributeResponse response = dictionaryService.createAttribute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Attribute created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all standard attributes", description = "Retrieves all standard attributes with nested canonical values")
    public ResponseEntity<ApiResponse<List<AttributeResponse>>> getAllAttributes() {
        List<AttributeResponse> response = dictionaryService.getAllAttributes();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attribute by ID", description = "Retrieves attribute details by ID")
    public ResponseEntity<ApiResponse<AttributeResponse>> getAttributeById(@PathVariable Long id) {
        AttributeResponse response = dictionaryService.getAttributeById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/values")
    @Operation(summary = "Add value to attribute", description = "Adds a canonical value option (e.g. RED, XL) to an attribute")
    public ResponseEntity<ApiResponse<AttributeValueResponse>> addAttributeValue(
            @PathVariable Long id,
            @Valid @RequestBody CreateAttributeValueRequest request) {
        AttributeValueResponse response = dictionaryService.addAttributeValue(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Attribute value created successfully", response));
    }

    @GetMapping("/{id}/values")
    @Operation(summary = "List values of an attribute", description = "Retrieves all canonical value options for an attribute")
    public ResponseEntity<ApiResponse<List<AttributeValueResponse>>> getAttributeValues(@PathVariable Long id) {
        List<AttributeValueResponse> response = dictionaryService.getAttributeValues(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/aliases")
    @Operation(summary = "Add alias to attribute", description = "Maps vendor raw attribute name to this canonical attribute")
    public ResponseEntity<ApiResponse<AttributeAliasResponse>> addAttributeAlias(
            @PathVariable Long id,
            @Valid @RequestBody CreateAttributeAliasRequest request) {
        AttributeAliasResponse response = dictionaryService.addAttributeAlias(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Attribute alias created successfully", response));
    }

    @GetMapping("/{id}/aliases")
    @Operation(summary = "List aliases of an attribute", description = "Retrieves all vendor name aliases mapped to this attribute")
    public ResponseEntity<ApiResponse<List<AttributeAliasResponse>>> getAttributeAliases(@PathVariable Long id) {
        List<AttributeAliasResponse> response = dictionaryService.getAttributeAliases(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{attrId}/values/{valId}/aliases")
    @Operation(summary = "Add alias to attribute value", description = "Maps vendor raw value to this canonical attribute value with composite foreign key integrity")
    public ResponseEntity<ApiResponse<AttributeValueAliasResponse>> addAttributeValueAlias(
            @PathVariable Long attrId,
            @PathVariable Long valId,
            @Valid @RequestBody CreateAttributeValueAliasRequest request) {
        AttributeValueAliasResponse response = dictionaryService.addAttributeValueAlias(attrId, valId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Attribute value alias created successfully", response));
    }

    @GetMapping("/{attrId}/values/{valId}/aliases")
    @Operation(summary = "List aliases of an attribute value", description = "Retrieves all vendor raw values mapped to this canonical value")
    public ResponseEntity<ApiResponse<List<AttributeValueAliasResponse>>> getAttributeValueAliases(
            @PathVariable Long attrId,
            @PathVariable Long valId) {
        List<AttributeValueAliasResponse> response = dictionaryService.getAttributeValueAliases(attrId, valId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
