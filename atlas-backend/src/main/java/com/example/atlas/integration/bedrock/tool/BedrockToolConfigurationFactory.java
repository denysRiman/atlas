package com.example.atlas.integration.bedrock.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockruntime.model.Tool;
import software.amazon.awssdk.services.bedrockruntime.model.ToolConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.ToolInputSchema;
import software.amazon.awssdk.services.bedrockruntime.model.ToolSpecification;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BedrockToolConfigurationFactory {
    public ToolConfiguration createToolConfiguration() {
        return ToolConfiguration.builder()
                .tools(
                        Tool.builder()
                                .toolSpec(createWeatherToolSpecification())
                                .build())
                .build();
    }

    private ToolSpecification createWeatherToolSpecification() {
        ToolInputSchema weatherInputSchema = ToolInputSchema.builder()
                .json(Document.fromMap(Map.of(
                        "type", Document.fromString("object"),
                        "properties", Document.fromMap(Map.of(
                                "city", Document.fromMap(Map.of(
                                        "type", Document.fromString("string"),
                                        "description", Document.fromString("City to get current weather for")
                                ))
                        )),
                        "required", Document.fromList(List.of(
                                Document.fromString("city")
                        ))
                )))
                .build();

        return ToolSpecification.builder()
                .name("getWeather")
                .description("Get current weather for a city")
                .inputSchema(weatherInputSchema).build();
    }
}
