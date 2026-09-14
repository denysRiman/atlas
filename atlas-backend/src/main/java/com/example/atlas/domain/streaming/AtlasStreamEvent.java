package com.example.atlas.domain.streaming;

import com.example.atlas.api.dto.InferenceResponse;

public sealed interface AtlasStreamEvent permits StreamStartedEvent, StreamDeltaEvent,
        StreamCompletedEvent, StreamErrorEvent {
}
