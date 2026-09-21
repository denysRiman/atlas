package com.example.atlas.domain.conversation;

import tools.jackson.databind.JsonNode;

public record ToolUseContent(String toolUseId, String toolName, JsonNode input) implements ConversationContent {
}
