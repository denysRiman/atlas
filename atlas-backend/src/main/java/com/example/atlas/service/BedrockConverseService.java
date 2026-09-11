package com.example.atlas.service;

import com.example.atlas.dto.ConversationMessage;
import com.example.atlas.dto.IncomeMessage;
import com.example.atlas.dto.ModelConfigs;
import com.example.atlas.dto.OutcomeMessage;
import com.example.atlas.enums.Role;
import com.example.atlas.exception.InternalException;
import com.example.atlas.exception.ProviderAuthenticationException;
import com.example.atlas.exception.ProviderMisconfigurationException;
import com.example.atlas.exception.ProviderTimeoutException;
import com.example.atlas.utility.SystemPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BedrockConverseService {

    private static final String MODEL_ID = "eu.amazon.nova-micro-v1:0";
    //private final String MODEL_ID = "openai.gpt-oss-120b-1:0";


    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final SystemPromptProvider systemPromptProvider;


    public OutcomeMessage converse(List<ConversationMessage> conversationHistory, ModelConfigs modelConfigs) {
        try {
            ConverseResponse response = bedrockRuntimeClient.converse(
                    request -> request.modelId(MODEL_ID).messages(mapConversationHistory(conversationHistory))
                            .system(SystemContentBlock.fromText(systemPromptProvider.getSystemPrompt()))
                            .guardrailConfig(config -> config.guardrailIdentifier("d6cfaesnf76z")
                                    .guardrailVersion("4"))
                            .inferenceConfig(
                                    config -> config.maxTokens(modelConfigs.getMaxTokens())
                                            .temperature(modelConfigs.getTemperature())
                            )
            );
            return new OutcomeMessage(response.output().message().content().getFirst().text());
        } catch (AccessDeniedException e) {
            throw new ProviderAuthenticationException(e.getMessage());
        } catch (ModelTimeoutException e) {
            throw new ProviderTimeoutException(e.getMessage());
        } catch (ModelErrorException e) {
            throw new ProviderMisconfigurationException(e.getMessage());
        } catch (Error e) {
            throw new InternalException(e.getMessage());
        }
    }

    private List<Message> mapConversationHistory(List<ConversationMessage> conversationMessages) {
        List<Message> result = new ArrayList<>();
        for (ConversationMessage conversationMessage : conversationMessages) {
            Message message = Message.builder().content(ContentBlock.fromText(conversationMessage.getContent()))
                    .role(getConversationRole(conversationMessage.getRole())).build();
            result.add(message);
        }
        return result;
    }

    private ConversationRole getConversationRole(Role role) {
        return switch (role) {
            case USER -> ConversationRole.USER;
            case ASSISTANT -> ConversationRole.ASSISTANT;
        };
    }
}
