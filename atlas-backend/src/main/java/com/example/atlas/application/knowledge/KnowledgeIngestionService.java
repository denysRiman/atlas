package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.SourceDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeIngestionService {

    private final DocumentLoader documentLoader;
    private final Chunker chunker;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public void ingest() {
        var documents = documentLoader.load();
        for (SourceDocument sourceDocument: documents) {
            var chunks = chunker.chunk(sourceDocument);
            for (DocumentChunk chunk: chunks) {
                var embedding = embeddingService.embed(chunk.content());
                vectorStore.save(chunk, embedding);
            }

        }
    }
}
