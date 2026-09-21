package com.example.atlas.integration.bedrock.request;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.integration.bedrock.mapper.BedrockMessageMapper;
import com.example.atlas.integration.bedrock.tool.BedrockToolConfigurationFactory;
import com.example.atlas.prompt.SystemPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BedrockRequestFactory {

    private static final String MODEL_ID = "eu.amazon.nova-micro-v1:0";

    private static final String GUARDRAIL_IDENTIFIER = "d6cfaesnf76z";
    private static final String GUARDRAIL_VERSION = "4";

    private final BedrockMessageMapper messageMapper;
    private final SystemPromptProvider systemPromptProvider;
    private final BedrockToolConfigurationFactory bedrockToolConfigurationFactory;

    public ConverseRequest createConverseRequest(List<ConversationMessage> conversationHistory, ModelConfig modelConfig) {
        return ConverseRequest.builder().modelId(MODEL_ID).messages(messageMapper.map(conversationHistory))
                .system(SystemContentBlock.fromText(systemPromptProvider.getSystemPrompt()))
                .toolConfig(bedrockToolConfigurationFactory.createToolConfiguration())
                .guardrailConfig(config -> config.guardrailIdentifier(GUARDRAIL_IDENTIFIER)
                        .guardrailVersion(GUARDRAIL_VERSION))
                .inferenceConfig(
                        config -> config.maxTokens(modelConfig.maxTokens())
                                .temperature(modelConfig.temperature())).build();
    }

    public ConverseStreamRequest createConverseStreamRequest(List<ConversationMessage> conversationHistory, ModelConfig modelConfig) {
        return ConverseStreamRequest.builder().modelId(MODEL_ID).messages(messageMapper.map(conversationHistory))
                .system(SystemContentBlock.fromText(systemPromptProvider.getSystemPrompt()))
                .guardrailConfig(config -> config.guardrailIdentifier(GUARDRAIL_IDENTIFIER)
                        .guardrailVersion(GUARDRAIL_VERSION))
                .inferenceConfig(
                        config -> config.maxTokens(modelConfig.maxTokens())
                                .temperature(modelConfig.temperature())).build();
    }

}
