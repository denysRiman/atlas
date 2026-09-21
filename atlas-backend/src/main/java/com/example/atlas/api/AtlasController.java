package com.example.atlas.api;

import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.api.dto.InferenceStreamResponse;
import com.example.atlas.api.dto.StreamResponseStatus;
import com.example.atlas.application.AtlasService;
import com.example.atlas.domain.streaming.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class AtlasController {

    @Value("${atlas.streaming.timeout}")
    private Duration timeout;

    private final AtlasService atlasService;

    @PostMapping("/inference")
    public ResponseEntity<InferenceResponse> inference(@Valid @RequestBody InferenceRequest request) {
        return ResponseEntity.ok(atlasService.inferentMessage(request));
    }

    @PostMapping(value = "/inference/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter inferenceStream(@Valid @RequestBody InferenceRequest request) {
        SseEmitter emitter = new SseEmitter(timeout.toMillis());
        var inference = atlasService.inferentMessageStream(request, event -> {
            sendEvent(event, emitter);
        });
        configureLifecycle(emitter, inference);
        return emitter;
    }

    private static void sendEvent(AtlasStreamEvent event, SseEmitter emitter) {
        InferenceStreamResponse response = switch (event) {
            case StreamStartedEvent ignored ->
                    new InferenceStreamResponse(StreamResponseStatus.START, null);
            case StreamDeltaEvent deltaEvent ->
                    new InferenceStreamResponse(StreamResponseStatus.DELTA, deltaEvent.getMessage());
            case StreamErrorEvent errorEvent ->
                    new InferenceStreamResponse(StreamResponseStatus.ERROR, errorEvent.getErrorMessage());
            case StreamCompletedEvent ignored ->
                    new InferenceStreamResponse(StreamResponseStatus.COMPLETED, null);
        };
        try {
            emitter.send(response);
            if (event instanceof StreamErrorEvent || event instanceof StreamCompletedEvent) {
                emitter.complete();
            }
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private static void configureLifecycle(SseEmitter emitter, CompletableFuture<Void> inference) {
        emitter.onTimeout(() -> {
            inference.cancel(false);
            emitter.complete();
        });
        emitter.onError(throwable -> {
            inference.cancel(false);
        });
    }
}
