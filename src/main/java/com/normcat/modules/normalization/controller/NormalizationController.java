package com.normcat.modules.normalization.controller;

import com.normcat.common.response.ApiResponse;
import com.normcat.modules.normalization.dto.request.NormalizationPreviewRequest;
import com.normcat.modules.normalization.dto.response.NormalizedAttributeResult;
import com.normcat.modules.normalization.service.NormalizationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/normalization")
@RequiredArgsConstructor
@Tag(name = "Normalization Engine", description = "Endpoints for previewing and verifying attribute normalization pipeline")
public class NormalizationController {

    private final NormalizationEngine normalizationEngine;

    @PostMapping("/preview")
    @Operation(summary = "Preview attribute normalization", description = "Evaluates a raw vendor key-value pair through the 2-phase resolution engine")
    public ResponseEntity<ApiResponse<NormalizedAttributeResult>> preview(@Valid @RequestBody NormalizationPreviewRequest request) {
        NormalizedAttributeResult result = normalizationEngine.resolve(request.getKey(), request.getValue());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
