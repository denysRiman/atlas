package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    @Value("${atlas.knowledge.retrieval.top-k}")
    int topK;

    @Value("${atlas.knowledge.retrieval.min-similarity}")
    double minSimilarity;

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public List<RetrievedChunk> retrieveKnowledge(String message) {
        var embedding = embeddingService.embed(message);
        return vectorStore.search(embedding, topK)
                .stream()
                .filter(retrievedChunk -> retrievedChunk.score() >= minSimilarity)
                .collect(Collectors.toList());
    }
}
