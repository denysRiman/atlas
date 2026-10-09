package com.example.atlas.integration.bedrock.knowledge;

import com.example.atlas.application.knowledge.KnowledgeRetriever;
import com.example.atlas.domain.knowledge.RetrievedChunk;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalResult;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.SearchType;

import java.util.List;

@ConditionalOnProperty(
        name = "atlas.knowledge.provider",
        havingValue = "bedrock"
)
@Service
@RequiredArgsConstructor
public class BedrockKnowledgeRetriever implements KnowledgeRetriever {

    private final BedrockAgentRuntimeClient bedrockAgentRuntimeClient;

    @Value("${atlas.knowledge.bedrock.knowledge-base-id}")
    private String knowledgeBaseId;

    @Override
    public List<RetrievedChunk> retrieve(String message, int topK) {
        var request = RetrieveRequest.builder()
                .knowledgeBaseId(knowledgeBaseId)
                .retrievalQuery(query -> query.text(message))
                .retrievalConfiguration(configuration ->
                        configuration.vectorSearchConfiguration(vectorSearch ->
                                vectorSearch
                                        .numberOfResults(topK)
                                        .overrideSearchType(SearchType.SEMANTIC)
                        )
                )
                .build();

        var response = bedrockAgentRuntimeClient.retrieve(request);

        return response.retrievalResults()
                .stream()
                .map(this::map)
                .toList();
    }

    private RetrievedChunk map(KnowledgeBaseRetrievalResult result) {
        var uri = result.location().s3Location().uri();

        return new RetrievedChunk(
                result.content().text(),
                uri.substring(uri.lastIndexOf('/') + 1),
                result.score()
        );
    }
}
