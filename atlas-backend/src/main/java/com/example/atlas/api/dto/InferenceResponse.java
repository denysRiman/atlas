package com.example.atlas.api.dto;

import java.util.List;

public record InferenceResponse(String message, List<String> sources) {
}