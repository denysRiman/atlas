package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.EmbeddedChunk;
import com.example.atlas.domain.knowledge.SourceDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
            List<EmbeddedChunk> embeddedChunks = new ArrayList<>();
            var chunks = chunker.chunk(sourceDocument);
            for (DocumentChunk chunk: chunks) {
                var embedding = embeddingService.embed(chunk.content());
                embeddedChunks.add(new EmbeddedChunk(chunk, embedding));
            }
            vectorStore.replaceDocumentChunks (sourceDocument.documentId(), embeddedChunks);
        }
    }
}
