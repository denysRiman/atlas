package com.example.atlas.domain.inference;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record ModelConfig(Integer maxTokens, Float temperature) {
}
