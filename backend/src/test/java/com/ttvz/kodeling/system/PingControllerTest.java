package com.ttvz.kodeling.system;

import com.ttvz.kodeling.config.ApiNoStoreCacheFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
 class PingControllerTest {
    private final MockMvc mockMvc;

    @Autowired
    PingControllerTest(WebApplicationContext context, ApiNoStoreCacheFilter apiNoStoreCacheFilter) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(apiNoStoreCacheFilter)
            .build();
    }

    @Test
    void pingReturns200WithNoStoreCacheControl() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void pingAppearsInOpenApiDocsAtRoot() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/v1/ping'].get.operationId").value("getPing"))
            .andExpect(jsonPath("$.paths['/api/v1/ping'].get.tags[0]").value("system"));
    }

    @Test
    void actuatorHealthRemainsAtRoot() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
