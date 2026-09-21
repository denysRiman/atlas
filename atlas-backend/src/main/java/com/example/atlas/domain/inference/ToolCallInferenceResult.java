package com.example.atlas.domain.inference;

import tools.jackson.databind.JsonNode;

public record ToolCallInferenceResult (String toolUseId, String toolName, JsonNode input) implements InferenceResult {
}
