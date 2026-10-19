package com.example.atlasmcpserver.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class TimeTool {

    @McpTool(name = "getTime", description = "Returns the current time for the requested IANA timezone",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public String getTime(@McpToolParam(description = "IANA timezone, for example Europe/Vienna", required = true) String timezone) {
        ZoneId zoneId =  ZoneId.of(timezone);
        return  ZonedDateTime.now(zoneId).toString();
    }

}
