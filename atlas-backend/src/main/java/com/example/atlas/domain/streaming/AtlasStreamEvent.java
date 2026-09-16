package com.example.atlas.domain.streaming;

public sealed interface AtlasStreamEvent permits StreamStartedEvent, StreamDeltaEvent,
        StreamCompletedEvent, StreamErrorEvent {
}
