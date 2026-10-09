package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.RetrievedChunk;

import java.util.List;

public interface KnowledgeRetriever {
    List<RetrievedChunk> retrieve(String message, int topK);
}
