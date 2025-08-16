package com.jie.aicode.ai.simpleAiCode;


import com.jie.aicode.utils.SpringContextUtil;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Configuration;

/**
 * aicode选择类型生成
 */
@Configuration
public class SimpleAiCodeFactory {


    public SimpleAiCodeService getAiCodeTypeChoice() {
        OpenAiChatModel simpleChatModel = SpringContextUtil.getBean("simpleChatModel", OpenAiChatModel.class);
        return AiServices.builder(SimpleAiCodeService.class)
                .chatModel(simpleChatModel)
                .build();
    }

    public SimpleAiCodeService getAppTitle() {
        OpenAiChatModel simpleChatModel = SpringContextUtil.getBean("simpleChatModel", OpenAiChatModel.class);
        return AiServices.builder(SimpleAiCodeService.class)
                .chatModel(simpleChatModel)
                .build();
    }


//    @Bean
//    public AiCodeTypeChoiceService getAiCodeTypeChoice() {
//        OpenAiChatModel simpleChatModel = SpringContextUtil.getBean("simpleChatModel", OpenAiChatModel.class);
//        return AiServices.builder(AiCodeTypeChoiceService.class)
//                .chatModel(simpleChatModel)
//                .build();
//    }
}
