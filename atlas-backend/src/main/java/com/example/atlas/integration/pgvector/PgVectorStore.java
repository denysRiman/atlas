package com.example.atlas.integration.pgvector;

import com.example.atlas.application.knowledge.VectorStore;
import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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
        if (queryEmbedding.length != 1024) {
            throw new IllegalArgumentException("Embedding must have 1024 dimensions, but was " + queryEmbedding.length);
        }

        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }

        return jdbcClient.sql("""
            SELECT
                chunk_id,
                document_id,
                content,
                chunk_index,
                source,
                1 - (embedding <=> CAST(:queryEmbedding AS vector)) AS score
            FROM document_chunk
            ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :topK
        """)
            .param("topK", topK)
            .param("queryEmbedding", toPgVector(queryEmbedding))
            .query((rs, rowNum) -> {
                var chunk = new DocumentChunk(
                        rs.getObject("chunk_id", UUID.class),
                        rs.getObject("document_id", UUID.class),
                        rs.getString("content"),
                        rs.getInt("chunk_index"),
                        rs.getString("source")
                );
                return new RetrievedChunk(chunk, rs.getDouble("score"));
            }).list();
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
