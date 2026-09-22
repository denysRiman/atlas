package com.example.atlas.application.tool.getWeather;

import org.springframework.stereotype.Component;

@Component
public class GetWeatherTool {
    public String execute(GetWeatherArguments weatherArguments) {
        return "The weather is sunny and has 18 degrees Celsius in" + weatherArguments.city();
    }
}
