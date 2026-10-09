package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {

    @Value("${atlas.knowledge.retrieval.top-k}")
    private int topK;

    private final KnowledgeRetriever knowledgeRetriever;

    public List<RetrievedChunk> retrieveKnowledge(String message) {
        return knowledgeRetriever.retrieve(message, topK);
    }
}
