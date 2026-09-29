package com.example.atlas.integration.bedrock.embedding;

import com.example.atlas.application.knowledge.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BedrockEmbeddingService implements EmbeddingService {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper;


    @Override
    public float[] embed(String text) {

        var payload = Map.of(
                "inputText", text,
                "dimensions", 1024,
                "normalize", true
        );
        byte[] jsonBytes = objectMapper.writeValueAsBytes(payload);
        SdkBytes body = SdkBytes.fromByteArray(jsonBytes);

        InvokeModelRequest modelRequest = InvokeModelRequest.builder()
                .modelId("amazon.titan-embed-text-v2:0")
                .contentType("application/json")
                .accept("application/json")
                .body(body)
                .build();
        var response = bedrockRuntimeClient.invokeModel(modelRequest);

        var responseJson = response.body().asUtf8String();
        var rootNode = objectMapper.readTree(responseJson);
        var embeddingNode = rootNode.get("embedding");
        float[] embedding = new float[embeddingNode.size()];

        if (!embeddingNode.isArray()) {
            throw new IllegalStateException("Titan response does not contain embedding array");
        }

        for(int i = 0; i < embeddingNode.size(); i++) {
            embedding[i] = embeddingNode.get(i).floatValue();
        }

        return embedding;
    }

}
