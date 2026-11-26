package com.ttvz.kodeling.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
class OpenApiConventionTest {

    private static final Set<String> HTTP_METHODS = Set.of(
        "get", "put", "post", "delete", "options", "head", "patch", "trace");

    private static final Pattern FEATURE_TAG_PATTERN = Pattern.compile("^[a-z][a-z0-9-]*$");

    private static final Pattern VERB_NOUN_CAMEL_CASE = Pattern.compile(
        "^(get|list|create|update|patch|delete|submit|reveal|block|unblock|"
            + "publish|archive|restore|run|dryRun|ping|check|verify|reset|change|select)"
            + "[A-Z][a-zA-Z0-9]*$");

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    private final MockMvc mockMvc;

    @Autowired
    OpenApiConventionTest(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void allOperationsFollowTagAndOperationIdConventions() throws Exception {
        String rawSpec = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode root = OBJECT_MAPPER.readTree(rawSpec);
        JsonNode paths = root.path("paths");
        assertThat(paths.isObject()).isTrue();
        assertThat(paths.size()).isGreaterThan(0);

        Set<String> seenOperationIds = new HashSet<>();

        paths.properties().forEach(pathEntry -> {
            String path = pathEntry.getKey();
            JsonNode pathItem = pathEntry.getValue();

            pathItem.properties().forEach(methodEntry -> {
                String method = methodEntry.getKey();
                if (!HTTP_METHODS.contains(method)) {
                    return;
                }
                JsonNode operation = methodEntry.getValue();
                String label = method.toUpperCase() + " " + path;

                JsonNode tags = operation.path("tags");
                assertThat(tags.isArray())
                    .as("Operation %s must declare tags", label)
                    .isTrue();
                List<String> tagValues = tags.values().stream().map(JsonNode::asString).toList();
                assertThat(tagValues)
                    .as("Operation %s must have exactly one feature tag", label)
                    .hasSize(1);
                assertThat(tagValues.getFirst())
                    .as("Tag on %s must be lowercase kebab-case", label)
                    .matches(FEATURE_TAG_PATTERN);

                String operationId = operation.path("operationId").asString("");
                assertThat(operationId)
                    .as("Operation %s must have a verbNoun camelCase operationId", label)
                    .matches(VERB_NOUN_CAMEL_CASE);
                assertThat(seenOperationIds.add(operationId))
                    .as("Duplicate operationId '%s' found on %s", operationId, label)
                    .isTrue();
            });
        });
    }
}
