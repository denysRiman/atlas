package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(
        name = "atlas.knowledge.provider",
        havingValue = "manual",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class ManualKnowledgeRetriever implements KnowledgeRetriever {

    @Value("${atlas.knowledge.retrieval.min-similarity}")
    private double minSimilarity;

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;


    @Override
    public List<RetrievedChunk> retrieve(String message, int topK) {
        var embedding = embeddingService.embed(message);
        return vectorStore.search(embedding, topK)
                .stream()
                .filter(retrievedChunk -> retrievedChunk.score() >= minSimilarity)
                .toList();
    }
}
