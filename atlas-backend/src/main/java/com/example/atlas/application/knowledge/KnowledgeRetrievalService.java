package com.example.atlas.application.knowledge;

import com.example.atlas.api.dto.InferenceRequest;
import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public List<RetrievedChunk> retrieveKnowledge(String message) {
        var embedding = embeddingService.embed(message);
        return vectorStore.search(embedding, 3);
    }
}
