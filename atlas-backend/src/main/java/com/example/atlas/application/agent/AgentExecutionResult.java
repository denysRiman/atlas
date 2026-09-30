package com.example.atlas.application.agent;

import com.example.atlas.domain.conversation.ConversationMessage;

import java.util.List;

public record AgentExecutionResult(String response, List<ConversationMessage> conversation, List<String> sources) {
}
