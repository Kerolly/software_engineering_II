package com.ttvz.kodeling.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import java.util.TreeMap;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kodelingOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Kodeling API")
                .version("v1")
                .description("REST API for the Kodeling Python learning platform"));
    }

    @Bean
    public OpenApiCustomizer deterministicSortCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                Paths sortedPaths = new Paths();
                new TreeMap<>(openApi.getPaths()).forEach(sortedPaths::addPathItem);
                openApi.setPaths(sortedPaths);
            }
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                openApi.getComponents().setSchemas(new TreeMap<>(openApi.getComponents().getSchemas()));
            }
        };
    }
}
