package com.example.atlas.api.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class InferenceRequest {

    @NotBlank
    @Size(max = 100)
    private String message;

    @NotNull
    private UUID conversationId;

    @JsonSetter(nulls = Nulls.SKIP)
    private Integer maxTokens = 300;

    @JsonSetter(nulls = Nulls.SKIP)
    private Float temperature = 0.2F;
}
