package com.example.atlas.integration.pgVector;

import com.example.atlas.application.knowledge.VectorStore;
import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class PgVectorStore implements VectorStore {

    private final JdbcClient jdbcClient;

    @Override
    public void save(DocumentChunk chunk, float[] embedding) {
        if (embedding.length != 1024) {
            throw new IllegalArgumentException("Embedding must have 1024 dimensions, but was " + embedding.length);
        }

        var vector = toPgVector(embedding);

        jdbcClient.sql("""
            INSERT INTO document_chunk (
                chunk_id,
                document_id,
                content,
                chunk_index,
                source,
                embedding
            )
            VALUES (
                :chunkId,
                :documentId,
                :content,
                :chunkIndex,
                :source,
                CAST(:embedding AS vector)
            )
            """)
                .param("chunkId", chunk.chunkId())
                .param("documentId", chunk.documentId())
                .param("content", chunk.content())
                .param("chunkIndex", chunk.chunkIndex())
                .param("source", chunk.source())
                .param("embedding", vector)
                .update();

    }

    @Override
    public List<RetrievedChunk> search(float[] queryEmbedding, int topK) {
        return List.of();
    }

    private static String toPgVector(float[] vector) {
        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < vector.length; i++) {
            if(i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        builder.append("]");
        return builder.toString();
    }
}
