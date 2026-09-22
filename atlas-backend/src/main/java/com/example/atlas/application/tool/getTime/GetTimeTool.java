package com.example.atlas.application.tool.getTime;

import org.springframework.stereotype.Component;

@Component
public class GetTimeTool {
    public String execute(GetTimeArguments timeArguments) {
        return "The time is 02:30 PM in " + timeArguments.city();
    }
}
