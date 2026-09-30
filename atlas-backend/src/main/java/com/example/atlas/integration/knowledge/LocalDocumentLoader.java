package com.example.atlas.integration.knowledge;

import com.example.atlas.application.knowledge.DocumentLoader;
import com.example.atlas.domain.knowledge.SourceDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalDocumentLoader implements DocumentLoader {

    @Value("${atlas.knowledge.location}")
    private String configLocation;

    private final ResourcePatternResolver resourcePatternResolver;

    @Override
    public List<SourceDocument> load() {
        List<SourceDocument> documents = new ArrayList<>();

        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources(configLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to resolve knowledge resources", e);
        }
        for (Resource resource : resources) {
            var filename = resource.getFilename();

            if (!resource.isReadable() || filename == null) {
                continue;
            }

            if (filename.endsWith(".txt") || filename.endsWith(".md")) {
                documents.add(createDocument(resource));
            }
        }
        return documents;
    }

    private SourceDocument createDocument(Resource resource) {
        String content;
        var fileName = resource.getFilename();
        try {
            content = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load document content",e);
        }
        return new SourceDocument(UUID.nameUUIDFromBytes(
                Objects.requireNonNull(fileName).getBytes(StandardCharsets.UTF_8)),
                fileName,
                content);
    }
}
