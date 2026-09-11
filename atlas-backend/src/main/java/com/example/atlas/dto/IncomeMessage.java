package com.example.atlas.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class IncomeMessage {

    @NotBlank
    @Size(max = 100)
    private String message;

    @JsonSetter(nulls = Nulls.SKIP)
    private Integer maxTokens = 300;

    @JsonSetter(nulls = Nulls.SKIP)
    private Float temperature = 0.2F;
}
