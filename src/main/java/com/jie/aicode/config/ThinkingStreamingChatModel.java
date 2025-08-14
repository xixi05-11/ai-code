package com.jie.aicode.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("langchain4j.open-ai.chat-model")
@Data
public class ThinkingStreamingChatModel {

    private String baseUrl;
    private String modelName;
    private String apiKey;

    /**
     * 创建深度思考流式模型 用于Vue项目
     *
     * @return
     */
    @Bean
    @Qualifier("thinkingStreamingChatModel")
    public StreamingChatModel thinkingChatModel() {
        //todo 暂时用v3 测试使用
//        modelName = "deepseek-r1";
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(8192)
                .logRequests(true)
                .logResponses(true)
                .build();

    }
}
