package com.example.atlas.utility;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Getter
@Component
public class SystemPromptProvider {

    private final String systemPrompt;

    public SystemPromptProvider(
            @Value("classpath:prompts/atlas-system-prompt.md") Resource systemPromptResource) {
        this.systemPrompt = loadPrompt(systemPromptResource);
    }

    private String loadPrompt(Resource resource) {
        try {
            String prompt = resource.getContentAsString(StandardCharsets.UTF_8).strip();

            if (prompt.isBlank()) {
                throw new IllegalStateException("System prompt is empty");
            }

            return prompt;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load system prompt", e);
        }
    }



}
