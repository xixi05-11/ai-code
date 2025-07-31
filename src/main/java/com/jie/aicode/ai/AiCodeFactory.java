package com.jie.aicode.ai;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建AI代码生成器的服务
     * @return
     */
//    @Bean
//    public AiCodeService aiCodeService(){
//        return AiServices.create(AiCodeService.class, chatModel);
//    }

    @Bean
    public AiCodeService aiCodeService(){
        return AiServices.builder(AiCodeService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
