package com.jie.aicode.ai.CodeTypeChoice;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeTypeChoiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeTypeChoiceService getAiCodeTypeChoice() {
        return AiServices.builder(AiCodeTypeChoiceService.class)
                .chatModel(chatModel)
                .build();
    }
}
