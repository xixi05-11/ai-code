package com.jie.aicode.ai;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jie.aicode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class AiCodeFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    //存储AI的服务
    Cache<Long, AiCodeService> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener((key, value, cause) -> {
                log.info("为 {} 的appId被移除,原因是:{}", key, cause);
            })
            .build();

    /**
     * 根据appId创建AI代码生成器的服务
     *
     * @param appId
     * @return
     */
    public AiCodeService getAiCodeService(long appId) {
        AiCodeService aiCodeService = cache.getIfPresent(appId);
        if (aiCodeService == null) {
            MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .id(appId)
                    //todo 暂且设置为10 少成本
                    .maxMessages(10)
                    .chatMemoryStore(redisChatMemoryStore)
                    .build();
            //将聊天记录保存到内存中
            chatHistoryService.saveChatHistoryToMemory(appId,chatMemory,10);

            aiCodeService = AiServices.builder(AiCodeService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
        }
        return aiCodeService;
    }

    /**
     * 创建AI代码生成器的服务
     *
     * @return
     */
    @Bean
    public AiCodeService aiCodeService() {
        return this.getAiCodeService(0L);
    }
}
