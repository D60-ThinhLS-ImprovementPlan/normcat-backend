package com.normcat.modules.normalization.service;

import com.normcat.modules.normalization.dto.response.NormalizedAttributeResult;

import java.util.List;
import java.util.Map;

/**
 * Deterministic Rule-based Normalization Engine.
 * Resolves multi-vendor raw attribute keys and values against the canonical alias dictionary.
 */
public interface NormalizationEngine {

    /**
     * Resolves a single raw key-value pair through the 2-phase resolution pipeline.
     *
     * @param rawKey   Raw vendor attribute key (e.g. "Màu sắc", "Colour")
     * @param rawValue Raw vendor attribute value (e.g. "Đỏ", "Red")
     * @return Resolved attribute result with status (NORMALIZED, PARTIALLY_NORMALIZED, UNKNOWN)
     */
    NormalizedAttributeResult resolve(String rawKey, String rawValue);

    /**
     * Resolves a collection of raw vendor attributes.
     *
     * @param rawAttributes Raw key-value map from product ingestion
     * @return List of resolved normalization results
     */
    List<NormalizedAttributeResult> resolveAll(Map<String, Object> rawAttributes);
}
