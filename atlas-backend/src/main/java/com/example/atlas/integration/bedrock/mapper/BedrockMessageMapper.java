package com.example.atlas.integration.bedrock.mapper;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.domain.conversation.Role;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.List;

@Component
public class BedrockMessageMapper {

    public List<Message> map(List<ConversationMessage> conversationHistory) {
        return conversationHistory.stream().map(this::mapMessage).toList();
    }

    private Message mapMessage(ConversationMessage conversationMessage){
        return Message.builder().content(ContentBlock.fromText(conversationMessage.content()))
                .role(mapRole(conversationMessage.role())).build();
    }

    private ConversationRole mapRole(Role role){
        return switch (role) {
            case USER -> ConversationRole.USER;
            case ASSISTANT -> ConversationRole.ASSISTANT;
        };
    }
}
