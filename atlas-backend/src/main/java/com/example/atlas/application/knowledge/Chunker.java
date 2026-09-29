package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.DocumentChunk;
import com.example.atlas.domain.knowledge.SourceDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class Chunker {

    @Value("${atlas.chunk.size}")
    private int chunkSize;

    @Value("${atlas.chunk.overlap}")
    private int chunkOverlap;

    public List<DocumentChunk> chunk(SourceDocument sourceDocument) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than 0");
        }

        if (chunkOverlap < 0 || chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "Chunk overlap must be >= 0 and smaller than chunk size"
            );
        }

        var content = sourceDocument.content();
        List<DocumentChunk> chunkList = new ArrayList<>();

        for (int chunkIndex = 0; ; chunkIndex++) {
            int start = chunkIndex * (chunkSize - chunkOverlap);

            if (start >= content.length()) {
                break;
            }

            int end = Math.min(start + chunkSize, content.length());
            int count = end - start;

            var chunkContent = String.copyValueOf(content.toCharArray(), start, count);

            chunkList.add(
                    new DocumentChunk(
                            UUID.randomUUID(),
                            sourceDocument.documentId(),
                            chunkContent,
                            chunkIndex,
                            sourceDocument.source()
                    )
            );

            if (end == content.length()) {
                break;
            }
        }

        return chunkList;
    }
}