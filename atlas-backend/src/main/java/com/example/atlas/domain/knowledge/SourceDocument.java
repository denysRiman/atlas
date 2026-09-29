package com.example.atlas.domain.knowledge;

import java.util.UUID;

public record SourceDocument(UUID documentId, String source, String content) {
}
