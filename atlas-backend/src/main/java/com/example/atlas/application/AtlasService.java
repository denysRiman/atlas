package com.example.atlas.application;

import com.example.atlas.application.agent.AgentRunner;
import com.example.atlas.application.conversation.ConversationStore;
import com.example.atlas.domain.conversation.*;
import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.domain.streaming.*;
import com.example.atlas.integration.bedrock.BedrockConverseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AtlasService {


    private final AgentRunner agentRunner;
    private final ConversationStore conversationStore;
    private final BedrockConverseService bedrockConverseService;


    public InferenceResponse inferentMessage(InferenceRequest request) {
        var modelConfig = new ModelConfig(request.getMaxTokens(), request.getTemperature());
        var snapshot = conversationStore.load(request.getConversationId());
        var executionResult = agentRunner.run(snapshot.conversationMessages(), request.getMessage(), modelConfig);

        conversationStore.save(request.getConversationId(), snapshot.version(), executionResult.conversation());
        return new InferenceResponse(executionResult.response());
    }

    public CompletableFuture<Void> inferentMessageStream(InferenceRequest request, Consumer<AtlasStreamEvent> eventConsumer) {
        var snapshot = conversationStore.load(request.getConversationId());
        var workingConversation = new ArrayList<>(snapshot.conversationMessages());
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
        var resultFuture = new CompletableFuture<Void>();
        conversedStream.whenComplete((result, exception) -> {
            if (!conversedStream.isCancelled()) {
                if (exception != null) {
                    eventConsumer.accept(new StreamErrorEvent(exception.getMessage()));
                    resultFuture.completeExceptionally(exception);
                } else {
                    workingConversation.add(new ConversationMessage(Role.ASSISTANT, new TextContent(assistantResponse.toString())));

                    try {
                        conversationStore.save(request.getConversationId(), snapshot.version(), workingConversation);
                    } catch (Exception saveException) {
                        eventConsumer.accept(new StreamErrorEvent(saveException.getMessage()));
                        resultFuture.completeExceptionally(saveException);
                        return;
                    }
                    eventConsumer.accept(new StreamCompletedEvent());
                    resultFuture.complete(null);
                }
            } else {
                resultFuture.cancel(false);
            }
        });
        resultFuture.whenComplete((result, exception) -> {
           if (resultFuture.isCancelled()) {
               conversedStream.cancel(false);
           }
        });

        return resultFuture;
    }
}
