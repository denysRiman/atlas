package com.example.atlas.domain.conversation;

public sealed interface ConversationContent permits TextContent, ToolUseContent,  ToolResultContent{
}
