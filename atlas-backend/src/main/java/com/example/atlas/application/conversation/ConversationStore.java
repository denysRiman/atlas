package com.example.atlas.application.conversation;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.domain.conversation.ConversationSnapshot;

import java.util.List;
import java.util.UUID;

public interface ConversationStore {
    ConversationSnapshot load(UUID conversationId);
    ConversationSnapshot save(UUID conversationId, long expectedVersion, List<ConversationMessage> messages);
}
