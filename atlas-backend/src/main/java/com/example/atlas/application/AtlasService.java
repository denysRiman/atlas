package com.example.atlas.application;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.domain.conversation.Role;
import com.example.atlas.domain.streaming.*;
import com.example.atlas.integration.bedrock.BedrockConverseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

    public CompletableFuture<Void> inferentMessageStream(InferenceRequest request, Consumer<AtlasStreamEvent> eventConsumer) {
        var workingConversation = new ArrayList<>(conversationHistory);
        workingConversation.add(new ConversationMessage(Role.USER, request.getMessage()));
        StringBuilder assistantResponse = new StringBuilder();

        Consumer<AtlasStreamEvent> internalEventConsumer = event -> {
            switch (event) {
                case StreamStartedEvent startedEvent:
                    eventConsumer.accept(startedEvent);
                    break;
                case StreamDeltaEvent streamDeltaEvent:
                    assistantResponse.append(streamDeltaEvent.getMessage());
                    eventConsumer.accept(streamDeltaEvent);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + event);
            }
        };

        var conversedStream = bedrockConverseService.converseStream(workingConversation,
                new ModelConfig(request.getMaxTokens(), request.getTemperature()), internalEventConsumer);
        conversedStream.whenComplete((result, exception) -> {
            if (!conversedStream.isCancelled()) {
                if (exception != null) {
                    eventConsumer.accept(new StreamErrorEvent(exception.getMessage()));
                } else {
                    conversationHistory.add(new ConversationMessage(Role.USER, request.getMessage()));
                    conversationHistory.add(new ConversationMessage(Role.ASSISTANT, assistantResponse.toString()));
                    eventConsumer.accept(new StreamCompletedEvent());
                }
            }
        });

        return conversedStream;
    }
}
