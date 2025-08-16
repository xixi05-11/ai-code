package com.jie.aicode.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 选择模型 用于选择生成类型与标题生成
 */
@Configuration
@ConfigurationProperties("langchain4j.open-ai.simple-chat-model")
@Data
public class SimpleChatModelConfig {

    private String baseUrl;
    private String modelName;
    private String apiKey;
    private boolean logRequests;
    private boolean logResponses;


    /**
     * 选择默认模型 用于选择生成类型与标题生成
     * @return
     */
    @Bean
    @Scope("prototype")
    public ChatModel simpleChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();

    }
}
