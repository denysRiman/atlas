package com.example.atlas.domain.streaming;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class StreamErrorEvent implements AtlasStreamEvent {
    private String errorMessage;
}
