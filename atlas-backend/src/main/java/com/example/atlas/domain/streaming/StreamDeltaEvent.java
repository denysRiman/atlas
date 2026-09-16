package com.example.atlas.domain.streaming;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class StreamDeltaEvent implements AtlasStreamEvent {
    private String Message;
}
