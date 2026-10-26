package com.normcat.modules.normalization.model;

/**
 * Normalization resolution status.
 * <p>
 * NORMALIZED: Both standard attribute key and standard attribute value were deterministically resolved.
 * PARTIALLY_NORMALIZED: Standard attribute key was resolved, but raw value has no canonical alias mapping.
 * UNKNOWN: Raw attribute key was not recognized in the alias dictionary or attribute catalog.
 */
public enum NormalizationStatus {
    NORMALIZED,
    PARTIALLY_NORMALIZED,
    UNKNOWN
}
