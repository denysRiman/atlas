package com.example.atlas.api;

import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.api.dto.InferenceResponse;
import com.example.atlas.api.dto.InferenceStreamResponse;
import com.example.atlas.application.AtlasService;
import com.example.atlas.domain.streaming.StreamCompletedEvent;
import com.example.atlas.domain.streaming.StreamDeltaEvent;
import com.example.atlas.domain.streaming.StreamErrorEvent;
import com.example.atlas.domain.streaming.StreamStartedEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AtlasController {

    private final AtlasService atlasService;

    @PostMapping("/inference")
    public ResponseEntity<InferenceResponse> inference(@Valid @RequestBody InferenceRequest message) {
        return ResponseEntity.ok(atlasService.inferentMessage(message));
    }

    @PostMapping(
            value = "/inference/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter inferenceStream(@RequestBody InferenceRequest request) {

        SseEmitter emitter = new SseEmitter(0L);

        atlasService.inferentMessageStream(request, event -> {
            InferenceStreamResponse response = switch (event) {
                case StreamStartedEvent ignored ->
                        new InferenceStreamResponse("start", null);

                case StreamDeltaEvent delta ->
                        new InferenceStreamResponse("delta", delta.getMessage());

                case StreamErrorEvent ignored ->
                        new InferenceStreamResponse("error", null);

                case StreamCompletedEvent ignored ->
                        new InferenceStreamResponse("completed", null);
            };

            try {
                emitter.send(response);

                if (event instanceof StreamErrorEvent
                        || event instanceof StreamCompletedEvent) {
                    emitter.complete();
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
