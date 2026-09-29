package com.example.atlas.application.agent;

import com.example.atlas.application.knowledge.KnowledgeRetrievalService;
import com.example.atlas.application.tool.ToolExecutor;
import com.example.atlas.domain.conversation.*;
import com.example.atlas.domain.inference.InferenceResult;
import com.example.atlas.domain.inference.ModelConfig;
import com.example.atlas.domain.inference.TextInferenceResult;
import com.example.atlas.domain.inference.ToolCallInferenceResult;
import com.example.atlas.integration.bedrock.BedrockConverseService;
import com.example.atlas.prompt.KnowledgePromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentRunner {

    @Value("${atlas.max.agent.steps}")
    private int maxSteps;

    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final KnowledgePromptProvider knowledgePromptProvider;
    private final BedrockConverseService bedrockConverseService;
    private final ToolExecutor toolExecutor;

    public AgentExecutionResult run(List<ConversationMessage> existingMessages, String message, ModelConfig modelConfig) {
        var persistentConversation = new ArrayList<>(existingMessages);
        persistentConversation.add(
                new ConversationMessage(
                        Role.USER,
                        new TextContent(message)
                )
        );

        var knowledge = knowledgeRetrievalService.retrieveKnowledge(message);
        var modelConversation = new ArrayList<>(existingMessages);
        modelConversation.add(
                new ConversationMessage(
                        Role.USER,
                        new TextContent(
                                knowledgePromptProvider.loadKnowledge(
                                        message,
                                        knowledge
                                )
                        )
                )
        );

        for(int step = 0; step < maxSteps; step++) {
            InferenceResult inferenceResult = bedrockConverseService.converse(modelConversation, modelConfig);

            switch (inferenceResult) {
                case TextInferenceResult result:
                    persistentConversation.add(new ConversationMessage(Role.ASSISTANT, new TextContent(result.text())));
                    return new AgentExecutionResult(result.text(), persistentConversation);
                case ToolCallInferenceResult result:
                    ConversationMessage assistantMessage = new ConversationMessage(Role.ASSISTANT, new ToolUseContent(result.toolUseId(), result.toolName(), result.input()));
                    persistentConversation.add(assistantMessage);
                    modelConversation.add(assistantMessage);
                    var toolResult = toolExecutor.execute(result.toolName(), result.input());
                    ConversationMessage toolMessage = new ConversationMessage(Role.USER, new ToolResultContent(result.toolUseId(), toolResult));
                    persistentConversation.add(toolMessage);
                    modelConversation.add(toolMessage);
            }
        }
        throw new RuntimeException("Maximum agent steps exceeded");
    }
}
