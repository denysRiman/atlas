package com.example.atlas.application.knowledge;

import com.example.atlas.domain.knowledge.SourceDocument;

import java.util.List;

public interface DocumentLoader {

    public List<SourceDocument> load();
}
