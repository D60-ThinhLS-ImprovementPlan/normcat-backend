package com.normcat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NormCatApplicationTests {

    @Test
    @DisplayName("Should successfully load Spring Application Context")
    void contextLoads() {
        // Verifies that the complete application context wires up without bean definition or configuration issues.
    }
}
