package com.normcat.common.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Text Preprocessing Pipeline utility for consistent attribute/value normalization.
 * <p>
 * Ensures 100% deterministic matching between vendor input strings and database aliases:
 * 1. Unicode Normalization Form C (NFC) - eliminates precomposed vs decomposed character mismatch.
 * 2. Collapse contiguous whitespace into a single space and trim margins.
 * 3. Convert to lowercase using root locale.
 */
public final class StringNormalizer {

    private StringNormalizer() {
        // Utility class
    }

    public static String normalize(String input) {
        if (input == null) {
            return null;
        }

        // Phase 1: Canonical Unicode Normalization (NFC)
        String nfc = Normalizer.normalize(input, Normalizer.Form.NFC);

        // Phase 2: Collapse multiple whitespace and trim
        String trimmed = nfc.trim().replaceAll("\\s+", " ");

        // Phase 3: Lowercase conversion
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
