package com.example.atlas.service;

import com.example.atlas.dto.ConversationMessage;
import com.example.atlas.dto.IncomeMessage;
import com.example.atlas.dto.ModelConfigs;
import com.example.atlas.dto.OutcomeMessage;
import com.example.atlas.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtlasService {

    private final BedrockConverseService bedrockConverseService;

    private final List<ConversationMessage> conversationHistory = new ArrayList<>();

    public OutcomeMessage inferentMessage(IncomeMessage message) {
        conversationHistory.add(new ConversationMessage(Role.USER, message.getMessage()));
        var converseResult = bedrockConverseService.converse(conversationHistory,
                new ModelConfigs(message.getMaxTokens(), message.getTemperature()));
        conversationHistory.add(new ConversationMessage(Role.ASSISTANT, converseResult.getMessage()));
        return converseResult;
    }
}
