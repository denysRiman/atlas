package com.example.atlas.prompt;

import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class KnowledgePromptProvider {

    @Value("classpath:prompts/atlas-knowledge-prompt.md")
    Resource knowledgePromptResource;

    public String loadKnowledge(String question, List<RetrievedChunk> retrievedChunks) {
        try {
            String knowledgeTemplate = knowledgePromptResource.getContentAsString(StandardCharsets.UTF_8).strip();

            if (knowledgeTemplate.isBlank()) {
                throw new IllegalStateException("Knowledge prompt is empty");
            }

            var knowledgeBuilder = new StringBuilder();
            for (RetrievedChunk retrievedChunk : retrievedChunks) {
                var chunk = retrievedChunk.documentChunk();

                knowledgeBuilder
                        .append("[Source: ")
                        .append(chunk.source())
                        .append(", chunk: ")
                        .append(chunk.chunkIndex())
                        .append("]\n")
                        .append(chunk.content())
                        .append("\n\n");
            }
            var knowledge = knowledgeBuilder.toString().strip();

            return knowledgeTemplate
                    .replace("{{knowledge}}", knowledge)
                    .replace("{{question}}", question);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load knowledge prompt", e);
        }
    }
}
