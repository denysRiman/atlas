package com.example.atlas.application.conversation;

import com.example.atlas.domain.conversation.ConversationMessage;
import com.example.atlas.domain.conversation.ConversationSnapshot;
import com.example.atlas.exception.ConversationVersionConflictException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryConversationStore implements ConversationStore {

    private final ConcurrentHashMap<UUID, ConversationSnapshot> conversationStore = new ConcurrentHashMap<>();

    @Override
    public ConversationSnapshot load(UUID conversationId) {
        return conversationStore.getOrDefault(conversationId, new ConversationSnapshot(List.of(), 0));
    }

    @Override
    public ConversationSnapshot save(UUID conversationId, long expectedVersion, List<ConversationMessage> messages) {
        return conversationStore.compute(conversationId, (id, currentSnapshot) -> {
            if (currentSnapshot == null) {
                if (expectedVersion == 0) {
                    return new ConversationSnapshot(messages, 1);
                } else {
                    throw new ConversationVersionConflictException("Expected version has to be 0");
                }
            } else {
                if (currentSnapshot.version() == expectedVersion) {
                    return new ConversationSnapshot(messages, expectedVersion + 1);
                } else {
                    throw new ConversationVersionConflictException("Concurrent conversation message save");
                }
            }
        });
    }
}
