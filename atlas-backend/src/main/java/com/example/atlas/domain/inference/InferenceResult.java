package com.example.atlas.domain.inference;

public sealed interface InferenceResult permits TextInferenceResult, ToolCallInferenceResult {
}
