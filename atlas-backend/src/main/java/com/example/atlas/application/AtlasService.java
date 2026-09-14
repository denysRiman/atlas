package com.example.atlas.application;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.domain.conversation.Role;
import com.example.atlas.integration.bedrock.BedrockConverseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtlasService {

    private final BedrockConverseService bedrockConverseService;

    private final List<ConversationMessage> conversationHistory = new ArrayList<>();

    public InferenceResponse inferentMessage(InferenceRequest message) {
        conversationHistory.add(new ConversationMessage(Role.USER, message.getMessage()));
        var converseResult = bedrockConverseService.converse(conversationHistory,
                new ModelConfig(message.getMaxTokens(), message.getTemperature()));
        conversationHistory.add(new ConversationMessage(Role.ASSISTANT, converseResult.getMessage()));
        return converseResult;
    }
}
