package com.example.atlas.application;

import com.example.atlas.application.tool.ToolExecutor;
import com.example.atlas.domain.conversation.*;
import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.domain.inference.InferenceResult;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.domain.inference.TextInferenceResult;
import com.example.atlas.domain.inference.ToolCallInferenceResult;
import com.example.atlas.domain.streaming.*;
import com.example.atlas.integration.bedrock.BedrockConverseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AtlasService {

    @Value("${atlas.max.agent.steps}")
    private int maxSteps;

    private final BedrockConverseService bedrockConverseService;
    private final ToolExecutor toolExecutor;

    private final List<ConversationMessage> conversationHistory = new ArrayList<>();

    public InferenceResponse inferentMessage(InferenceRequest request) {
        var modelConfig = new ModelConfig(request.getMaxTokens(), request.getTemperature());

        var workingConversation = new ArrayList<>(conversationHistory);
        workingConversation.add(new ConversationMessage(Role.USER, new TextContent(request.getMessage())));

        for(int step = 0; step < maxSteps; step++) {
            InferenceResult inferenceResult = bedrockConverseService.converse(workingConversation, modelConfig);

            switch (inferenceResult) {
                case TextInferenceResult result:
                    workingConversation.add(new ConversationMessage(Role.ASSISTANT, new TextContent(result.text())));
                    commitConversation(workingConversation);
                    return new InferenceResponse(result.text());
                case ToolCallInferenceResult result:
                    workingConversation.add(new ConversationMessage(Role.ASSISTANT, new ToolUseContent(result.toolUseId(), result.toolName(), result.input())));
                    var toolResult = toolExecutor.execute(result.toolName(), result.input());
                    workingConversation.add(new ConversationMessage(Role.USER, new ToolResultContent(result.toolUseId(), toolResult)));
            }
        }
        throw new RuntimeException("Maximum agent steps exceeded");

    }

    public CompletableFuture<Void> inferentMessageStream(InferenceRequest request, Consumer<AtlasStreamEvent> eventConsumer) {
        var workingConversation = new ArrayList<>(conversationHistory);
        workingConversation.add(new ConversationMessage(Role.USER, new TextContent(request.getMessage())));
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
                    conversationHistory.add(new ConversationMessage(Role.USER, new TextContent(request.getMessage())));
                    conversationHistory.add(new ConversationMessage(Role.ASSISTANT, new TextContent(assistantResponse.toString())));
                    eventConsumer.accept(new StreamCompletedEvent());
                }
            }
        });

        return conversedStream;
    }

    private void commitConversation(List<ConversationMessage> workingConversation) {
        conversationHistory.clear();
        conversationHistory.addAll(workingConversation);
    }
}
