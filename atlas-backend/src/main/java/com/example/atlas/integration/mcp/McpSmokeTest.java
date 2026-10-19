package com.example.atlas.integration.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class McpSmokeTest implements CommandLineRunner {

    private final List<McpSyncClient> mcpSyncClients;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("MCP CLIENT COUNT = " + mcpSyncClients.size());

        for (McpSyncClient syncClient: mcpSyncClients) {
            var result = syncClient.listTools();

            for(McpSchema.Tool tool: result.tools()) {
                System.out.println(tool.name());
                System.out.println(tool.description());
                System.out.println(tool.inputSchema());
                System.out.println(tool.annotations());
            }
        }
    }
}
