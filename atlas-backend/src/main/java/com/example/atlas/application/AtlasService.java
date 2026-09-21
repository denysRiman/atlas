package com.example.atlas.application;

import com.example.atlas.application.tool.GetWeatherArguments;
import com.example.atlas.application.tool.GetWeatherTool;
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
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AtlasService {

    private final ObjectMapper objectMapper;

    private final BedrockConverseService bedrockConverseService;
    private final GetWeatherTool getWeatherTool;

    private final List<ConversationMessage> conversationHistory = new ArrayList<>();

    public InferenceResponse inferentMessage(InferenceRequest request) {
        var modelConfig = new ModelConfig(request.getMaxTokens(), request.getTemperature());

        var workingConversation = new ArrayList<>(conversationHistory);
        workingConversation.add(new ConversationMessage(Role.USER, new TextContent(request.getMessage())));

        InferenceResult inferenceResult = bedrockConverseService.converse(workingConversation, modelConfig);

        switch (inferenceResult) {
            case TextInferenceResult result:
                workingConversation.add(new ConversationMessage(Role.ASSISTANT, new TextContent(result.text())));
                commitConversation(workingConversation);
                return new InferenceResponse(result.text());
            case ToolCallInferenceResult result:
                workingConversation.add(new ConversationMessage(Role.ASSISTANT, new ToolUseContent(result.toolUseId(), result.toolName(), result.input())));
                if (Objects.equals(result.toolName(), "getWeather")) {
                    var toolResult = getWeatherTool.execute(objectMapper.treeToValue(result.input(), GetWeatherArguments.class));
                    workingConversation.add(new ConversationMessage(Role.USER, new ToolResultContent(result.toolUseId(), toolResult)));
                    InferenceResult secondInferenceResult = bedrockConverseService.converse(workingConversation, modelConfig);
                    return switch (secondInferenceResult) {
                        case TextInferenceResult textInferenceResult -> {
                            workingConversation.add(new ConversationMessage(Role.ASSISTANT, new TextContent(textInferenceResult.text())));
                            commitConversation(workingConversation);
                            yield new InferenceResponse(textInferenceResult.text());
                        }
                        case ToolCallInferenceResult toolCallInferenceResult ->
                                throw new UnsupportedOperationException("Multiple tool calls are not supported yet");
                    };
                } else {
                    throw new UnsupportedOperationException(
                            "Unsupported tool: " + result.toolName()
                    );
                }

        }
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
