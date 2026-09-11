package com.example.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModelConfigs {
    private Integer maxTokens;
    private Float temperature;
}
