package com.normcat.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.Normalizer;

import static org.assertj.core.api.Assertions.assertThat;

class StringNormalizerTest {

    @ParameterizedTest(name = "normalize(\"{0}\") should return \"{1}\"")
    @CsvSource({
            "RAM, ram",
            "ram, ram",
            "'Ram ', ram",
            "'  RAM  ', ram",
            "'   Bộ    nhớ    trong   ', bộ nhớ trong",
            "'Màu sắc', màu sắc",
            "'  8GB  ', 8gb",
            "'Apple iPhone 15 Pro', apple iphone 15 pro"
    })
    @DisplayName("Should normalize case and whitespace correctly")
    void shouldNormalizeCaseAndWhitespace(String input, String expected) {
        assertThat(StringNormalizer.normalize(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should unify decomposed Unicode (NFD) to canonical composed Unicode (NFC)")
    void shouldUnifyDecomposedUnicodeToNfc() {
        // Vietnamese "Màu sắc" in NFD (decomposed: base letters + combining diacritical marks)
        String nfdInput = Normalizer.normalize("Màu sắc", Normalizer.Form.NFD);
        // Vietnamese "Màu sắc" in NFC (composed)
        String nfcExpected = Normalizer.normalize("màu sắc", Normalizer.Form.NFC);

        // Make sure the input was indeed physically decomposed
        assertThat(nfdInput).isNotEqualTo(nfcExpected);

        // StringNormalizer must output identical NFC string
        String normalized = StringNormalizer.normalize(nfdInput);
        assertThat(normalized).isEqualTo(nfcExpected);
    }

    @Test
    @DisplayName("Should handle null, blank, and empty strings safely")
    void shouldHandleNullAndEmpty() {
        assertThat(StringNormalizer.normalize(null)).isNull();
        assertThat(StringNormalizer.normalize("")).isEmpty();
        assertThat(StringNormalizer.normalize("   ")).isEmpty();
    }
}
