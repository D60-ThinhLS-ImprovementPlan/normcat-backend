package com.normcat.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RestController
    static class TestController {

        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Category", "slug", "electronics");
        }

        @GetMapping("/test/conflict")
        public void throwConflict() {
            throw new ConflictException("SKU", "sku", "IPHONE-15");
        }

        @GetMapping("/test/business")
        public void throwBusiness() {
            throw new BusinessException(ErrorCode.SINGLE_VALUE_VIOLATION, "Attribute COLOR is single-valued");
        }

        @GetMapping("/test/data-integrity")
        public void throwDataIntegrity() {
            throw new DataIntegrityViolationException("Duplicate key constraint violation");
        }

        @GetMapping("/test/unhandled")
        public void throwUnhandled() {
            throw new RuntimeException("Unexpected memory crash");
        }

        @PostMapping("/test/validation")
        public void validatePayload(@Valid @RequestBody TestPayload payload) {
            // No-op
        }
    }

    @Getter
    @Setter
    static class TestPayload {
        @NotBlank(message = "Name must not be blank")
        private String name;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return HTTP 404 with RFC 7807 problem details when ResourceNotFoundException is thrown")
    void handleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Category not found with slug: 'electronics'")))
                .andExpect(jsonPath("$.path", is("/test/not-found")));
    }

    @Test
    @DisplayName("Should return HTTP 409 when ConflictException is thrown")
    void handleConflictException() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")))
                .andExpect(jsonPath("$.message", is("SKU already exists with sku: 'IPHONE-15'")));
    }

    @Test
    @DisplayName("Should return HTTP 400 when BusinessException is thrown with custom error code")
    void handleBusinessException() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.code", is("SINGLE_VALUE_VIOLATION")))
                .andExpect(jsonPath("$.message", is("Attribute COLOR is single-valued")));
    }

    @Test
    @DisplayName("Should return HTTP 409 when DataIntegrityViolationException is thrown")
    void handleDataIntegrityViolationException() throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.code", is("DUPLICATE_RESOURCE")));
    }

    @Test
    @DisplayName("Should return HTTP 400 with field errors when payload validation fails")
    void handleValidationException() throws Exception {
        TestPayload invalidPayload = new TestPayload(); // name is null

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("name")))
                .andExpect(jsonPath("$.errors[0].message", is("Name must not be blank")));
    }

    @Test
    @DisplayName("Should return HTTP 500 with sanitized message when unhandled exception occurs")
    void handleUnhandledException() throws Exception {
        mockMvc.perform(get("/test/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.code", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("An unexpected internal error occurred. Please contact system administrator.")));
    }
}
