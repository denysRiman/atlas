package com.example.atlas.application.tool;

import com.example.atlas.application.tool.getTime.GetTimeArguments;
import com.example.atlas.application.tool.getTime.GetTimeTool;
import com.example.atlas.application.tool.getWeather.GetWeatherArguments;
import com.example.atlas.application.tool.getWeather.GetWeatherTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ObjectMapper objectMapper;

    private final GetWeatherTool getWeatherTool;
    private final GetTimeTool getTimeTool;

    public String execute(String toolName, JsonNode input) {
        return switch (toolName) {
            case "getWeather" ->
                    getWeatherTool.execute(objectMapper.treeToValue(input, GetWeatherArguments.class));
            case "getTime" ->
                    getTimeTool.execute(objectMapper.treeToValue(input, GetTimeArguments.class));
            default -> throw new UnsupportedOperationException("Unsupported tool: " + toolName);
        };
    }
}
