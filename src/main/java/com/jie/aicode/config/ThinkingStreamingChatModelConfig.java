package com.jie.aicode.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConfigurationProperties("langchain4j.open-ai.thinking-streaming-chat-model")
@Data
public class ThinkingStreamingChatModelConfig {

    private String baseUrl;
    private String modelName;
    private String apiKey;
    private double temperature;
    private int maxCompletionTokens;
    private int maxRetries;
    private int maxTokens;
    private boolean logRequests;
    private boolean logResponses;


    /**
     * 创建深度思考流式模型 用于Vue项目
     * @return
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel thinkingStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .temperature(temperature)
                .maxCompletionTokens(maxCompletionTokens)
                .build();

    }
}
