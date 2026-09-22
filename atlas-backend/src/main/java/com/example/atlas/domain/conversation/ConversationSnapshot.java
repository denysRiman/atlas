package com.example.atlas.domain.conversation;

import java.util.List;

public record ConversationSnapshot(
        List<ConversationMessage> conversationMessages, long version) {

    public ConversationSnapshot {
        conversationMessages = List.copyOf(conversationMessages);
    }

}
