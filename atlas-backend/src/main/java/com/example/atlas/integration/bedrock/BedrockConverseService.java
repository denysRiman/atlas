package com.example.atlas.integration.bedrock;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.domain.streaming.*;
import com.example.atlas.integration.bedrock.exception.BedrockExceptionTranslator;
import com.example.atlas.integration.bedrock.request.BedrockRequestFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class BedrockConverseService {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    private final BedrockRequestFactory requestFactory;
    private final BedrockExceptionTranslator exceptionTranslator;


    public InferenceResponse converse(List<ConversationMessage> conversationHistory, ModelConfig modelConfig) {
        return  exceptionTranslator.execute(() -> {
            ConverseResponse response = bedrockRuntimeClient.converse(requestFactory
                    .createConverseRequest(conversationHistory, modelConfig));
            return new InferenceResponse(response.output().message().content().getFirst().text());
        });
    }

    public CompletableFuture<Void> converseStream(List<ConversationMessage> conversationHistory, ModelConfig modelConfig, Consumer<AtlasStreamEvent> eventConsumer) {
        return  exceptionTranslator.execute(() -> {
            ConverseStreamResponseHandler handler = ConverseStreamResponseHandler.builder()
                    .subscriber(ConverseStreamResponseHandler.Visitor.builder()
                            .onMessageStart(event -> eventConsumer.accept(new StreamStartedEvent()))
                            .onContentBlockDelta(event -> eventConsumer.accept(new StreamDeltaEvent(event.delta().text())))
                            .build()
                    )
                    .build();
            return bedrockRuntimeAsyncClient.converseStream(requestFactory.createConverseStreamRequest(conversationHistory, modelConfig), handler);
        });
    }
}
