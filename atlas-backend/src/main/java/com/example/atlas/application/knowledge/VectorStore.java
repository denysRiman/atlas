package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.RetrievedChunk;

import java.util.List;

public interface VectorStore {

    public void save(DocumentChunk chunk, float[] embedding);

    public List<RetrievedChunk> search(float[] queryEmbedding, int topK);
}
