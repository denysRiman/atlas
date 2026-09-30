package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.EmbeddedChunk;
import com.example.atlas.domain.knowledge.RetrievedChunk;

import java.util.List;
import java.util.UUID;

public interface VectorStore {

    public List<RetrievedChunk> search(float[] queryEmbedding, int topK);

    public void replaceDocumentChunks(UUID documentId, List<EmbeddedChunk> newChunks);
}
