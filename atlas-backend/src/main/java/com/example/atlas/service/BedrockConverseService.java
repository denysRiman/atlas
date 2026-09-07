package com.example.atlas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

@Service
@RequiredArgsConstructor
public class BedrockConverseService {

    private static final String MODEL_ID = "eu.amazon.nova-micro-v1:0";
    //private final String MODEL_ID = "openai.gpt-oss-120b-1:0";



    private final BedrockRuntimeClient bedrockRuntimeClient;


    public String converse(String question) {
        Message message = Message.builder()
                .content(ContentBlock.fromText(question))
                .role(ConversationRole.USER).build();
        ConverseResponse response = bedrockRuntimeClient.converse(request -> request.modelId(MODEL_ID).messages(message));

        return response.output().message().content().getFirst().text();
    }
}
