package com.ttvz.kodeling.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
class OpenApiSnapshotTest {

    private static final Path SNAPSHOT_PATH = Path.of("openapi.json");

    private static final ObjectMapper CANONICAL_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build();

    private final MockMvc mockMvc;

    @Autowired
    OpenApiSnapshotTest(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void openApiMatchesCommittedSnapshot() throws Exception {
        String rawSpec = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode tree = CANONICAL_MAPPER.readTree(rawSpec);
        String actual = CANONICAL_MAPPER.writeValueAsString(tree).replace("\r\n", "\n") + "\n";

        if (Boolean.getBoolean("openapi.write")) {
            Files.writeString(SNAPSHOT_PATH, actual, StandardCharsets.UTF_8);
            return;
        }

        assertThat(SNAPSHOT_PATH)
            .as("backend/openapi.json does not exist. Run 'just gen-api' to generate it.")
            .exists();

        String expected = Files.readString(SNAPSHOT_PATH, StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertThat(actual)
            .as("backend/openapi.json is out of date with the API. Run 'just gen-api' to regenerate it.")
            .isEqualTo(expected);
    }
}
