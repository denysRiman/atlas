package com.example.atlas.integration.bedrock;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.domain.inference.InferenceResult;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.domain.streaming.*;
import com.example.atlas.integration.bedrock.exception.BedrockExceptionTranslator;
import com.example.atlas.integration.bedrock.mapper.BedrockInferenceResultMapper;
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
    private final BedrockInferenceResultMapper bedrockInferenceResultMapper;


    public InferenceResult converse(List<ConversationMessage> conversationHistory, ModelConfig modelConfig) {
        return  exceptionTranslator.execute(() -> {
            ConverseResponse response = bedrockRuntimeClient.converse(requestFactory
                    .createConverseRequest(conversationHistory, modelConfig));
            return bedrockInferenceResultMapper.map(response);
        });
    }

    public CompletableFuture<Void> converseStream(List<ConversationMessage> conversationHistory, ModelConfig modelConfig, Consumer<AtlasStreamEvent> eventConsumer) {
        ConverseStreamResponseHandler handler = ConverseStreamResponseHandler.builder()
                .subscriber(ConverseStreamResponseHandler.Visitor.builder()
                        .onMessageStart(event -> eventConsumer.accept(new StreamStartedEvent()))
                        .onContentBlockDelta(event -> eventConsumer.accept(new StreamDeltaEvent(event.delta().text())))
                        .build()
                )
                .build();
        var awsFuture = bedrockRuntimeAsyncClient.converseStream(requestFactory.createConverseStreamRequest(conversationHistory, modelConfig), handler);
        var resultFuture = new CompletableFuture<Void>();

        awsFuture.whenComplete((result, exception) -> {
           if (exception != null) {
               resultFuture.completeExceptionally(exceptionTranslator.translate(exception));
           } else {
               resultFuture.complete(null);
           }
        });

        resultFuture.whenComplete((r, e) -> {
           if(resultFuture.isCancelled()) {
               awsFuture.cancel(false);
           }
        });

        return resultFuture;
    }
}
