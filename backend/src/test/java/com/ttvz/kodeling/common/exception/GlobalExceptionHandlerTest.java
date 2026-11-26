package com.ttvz.kodeling.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ttvz.kodeling.config.ApiNoStoreCacheFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StubController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .addFilters(new ApiNoStoreCacheFilter())
        .build();

    record SampleRequest(@NotBlank(message = "must not be blank") String title) {
    }

    @RestController
    static class StubController {

        @PostMapping("/api/v1/test/validate")
        void validate(@Valid @RequestBody SampleRequest request) {
        }

        @GetMapping("/api/v1/test/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Problem", "missing-slug");
        }

        @GetMapping("/api/v1/test/conflict")
        void conflict() {
            throw new ConflictException("Problem is already published");
        }
    }

    @Test
    void validationFailureReturnsProblemDetailWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.type").value("https://kodeling/errors/validation"))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors[0].field").value("title"))
            .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));
    }

    @Test
    void notFoundReturnsProblemDetail404() throws Exception {
        mockMvc.perform(get("/api/v1/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.type").value("https://kodeling/errors/not-found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Problem not found: missing-slug"));
    }

    @Test
    void conflictReturnsProblemDetail409() throws Exception {
        mockMvc.perform(get("/api/v1/test/conflict"))
            .andExpect(status().isConflict())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.type").value("https://kodeling/errors/conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Problem is already published"));
    }
}
