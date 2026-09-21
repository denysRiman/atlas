package com.example.atlas.integration.bedrock.mapper;

import com.example.atlas.domain.inference.InferenceResult;
import com.example.atlas.domain.inference.TextInferenceResult;
import com.example.atlas.domain.inference.ToolCallInferenceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BedrockInferenceResultMapper {

    private final ObjectMapper objectMapper;

    public InferenceResult map(ConverseResponse converseResponse) {
        switch (converseResponse.stopReason()){
            case END_TURN -> {
                var text = converseResponse.output()
                        .message()
                        .content()
                        .stream()
                        .map(ContentBlock::text)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("END_TURN response contains no text block"));

                return new TextInferenceResult(text);
            }
            case TOOL_USE -> {
                var toolUse = converseResponse.output()
                        .message()
                        .content()
                        .stream()
                        .map(ContentBlock::toolUse)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("TOOL_USE stop reason contains no tool use block"));
                return new ToolCallInferenceResult(toolUse.toolUseId(), toolUse.name(), toJsonNode(toolUse.input()));
            }
            default -> throw new IllegalArgumentException("Unsupported AWS Stop Reason");
        }
    }

    private JsonNode toJsonNode(Document document) {
        if (document.isNull()) {
            return objectMapper.nullNode();
        }
        if (document.isString()) {
            return objectMapper.valueToTree(document.asString());
        }
        if (document.isBoolean()) {
            return objectMapper.valueToTree(document.asBoolean());
        }
        if (document.isNumber()) {
            return objectMapper.valueToTree(document.asNumber().bigDecimalValue());
        }
        if (document.isList()) {
            var array = objectMapper.createArrayNode();
            document.asList().forEach(item -> array.add(toJsonNode(item)));
            return array;
        }
        if (document.isMap()) {
            var object = objectMapper.createObjectNode();
            document.asMap().forEach((key, value) ->
                    object.set(key, toJsonNode(value))
            );
            return object;
        }

        throw new IllegalArgumentException("Unsupported AWS Document type");
    }
}
