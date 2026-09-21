package com.example.atlas.integration.bedrock.mapper;

import com.example.atlas.domain.conversation.*;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkNumber;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class BedrockMessageMapper {

    public List<Message> map(List<ConversationMessage> conversationHistory) {
        return conversationHistory.stream().map(this::mapMessage).toList();
    }

    private Message mapMessage(ConversationMessage conversationMessage){
        return Message.builder()
                .content(mapContent(conversationMessage.conversationMessage()))
                .role(mapRole(conversationMessage.role()))
                .build();
    }

    private ConversationRole mapRole(Role role){
        return switch (role) {
            case USER -> ConversationRole.USER;
            case ASSISTANT -> ConversationRole.ASSISTANT;
        };
    }

    private ContentBlock mapContent(ConversationContent conversationContent) {
        return switch (conversationContent) {
            case TextContent text ->
                ContentBlock.fromText(text.text());
            case ToolUseContent toolUse -> {
                var awsToolUse = ToolUseBlock.builder()
                        .toolUseId(toolUse.toolUseId())
                        .name(toolUse.toolName())
                        .input(toDocument(toolUse.input()))
                        .build();
                yield ContentBlock.builder()
                        .toolUse(awsToolUse)
                        .build();
            }
            case ToolResultContent toolResult -> {
                var resultContent = ToolResultContentBlock.builder()
                        .text(toolResult.result())
                        .build();

                var resultBlock = ToolResultBlock.builder()
                        .toolUseId(toolResult.toolUseId())
                        .content(resultContent)
                        .build();

                yield ContentBlock.builder()
                        .toolResult(resultBlock)
                        .build();
            }
        };
    }

    private Document toDocument(JsonNode node) {
        if (node.isNull()) {
            return Document.fromNull();
        }

        if (node.isString()) {
            return Document.fromString(node.asString());
        }

        if (node.isBoolean()) {
            return Document.fromBoolean(node.asBoolean());
        }

        if (node.isNumber()) {
            return Document.fromNumber(
                    SdkNumber.fromBigDecimal(node.decimalValue())
            );
        }

        if (node.isArray()) {
            var documents = new ArrayList<Document>();
            node.forEach(child -> documents.add(toDocument(child)));
            return Document.fromList(documents);
        }

        if (node.isObject()) {
            var documents = new LinkedHashMap<String, Document>();

            node.properties().forEach(entry ->
                    documents.put(
                            entry.getKey(),
                            toDocument(entry.getValue())
                    )
            );

            return Document.fromMap(documents);
        }

        throw new IllegalArgumentException(
                "Unsupported JSON node type: " + node.getNodeType()
        );
    }
}
