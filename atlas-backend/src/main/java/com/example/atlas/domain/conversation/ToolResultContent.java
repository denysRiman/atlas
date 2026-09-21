package com.example.atlas.domain.conversation;

public record ToolResultContent(String toolUseId, String result) implements ConversationContent {
}
