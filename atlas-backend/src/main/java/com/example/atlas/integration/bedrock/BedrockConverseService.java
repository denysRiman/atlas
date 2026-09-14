package com.example.atlas.integration.bedrock;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.integration.bedrock.exception.BedrockExceptionTranslator;
import com.example.atlas.integration.bedrock.mapper.BedrockMessageMapper;
import com.example.atlas.prompt.SystemPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BedrockConverseService {

    private static final String MODEL_ID = "eu.amazon.nova-micro-v1:0";

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final SystemPromptProvider systemPromptProvider;
    private final BedrockMessageMapper messageMapper;
    private final BedrockExceptionTranslator exceptionTranslator;


    public InferenceResponse converse(List<ConversationMessage> conversationHistory, ModelConfig modelConfig) {
        return  exceptionTranslator.execute(() -> {
            ConverseResponse response = bedrockRuntimeClient.converse(
                    request -> request.modelId(MODEL_ID).messages(messageMapper.map(conversationHistory))
                            .system(SystemContentBlock.fromText(systemPromptProvider.getSystemPrompt()))
                            .guardrailConfig(config -> config.guardrailIdentifier("d6cfaesnf76z")
                                    .guardrailVersion("4"))
                            .inferenceConfig(
                                    config -> config.maxTokens(modelConfig.maxTokens())
                                            .temperature(modelConfig.temperature())
                            ));
            return new InferenceResponse(response.output().message().content().getFirst().text());
        });
    }
}
