package com.jie.aicode.config;

import com.jie.aicode.monitor.AiModelListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * 创建流式的ai  多例
 */
@Configuration
@ConfigurationProperties("langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {

    private String baseUrl;
    private String modelName;
    private String apiKey;
    private double temperature;
    private int maxCompletionTokens;
    private int maxRetries;
    private int maxTokens;
    private boolean logRequests;
    private boolean logResponses;

    @Resource
    private AiModelListener aiModelListener;


    /**
     * 创建流式模型
     * @return
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel streamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .temperature(temperature)
                .maxCompletionTokens(maxCompletionTokens)
                .listeners(List.of(aiModelListener))
                .build();
    }
}
