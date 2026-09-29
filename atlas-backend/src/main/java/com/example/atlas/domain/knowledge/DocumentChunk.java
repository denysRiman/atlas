package com.example.atlas.domain.knowledge;

import java.util.UUID;

public record DocumentChunk(UUID chunkId, UUID documentId, String content, int chunkIndex, String source) {
}
