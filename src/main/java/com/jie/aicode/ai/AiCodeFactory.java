package com.jie.aicode.ai;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jie.aicode.ai.guardrail.PromptInputGuardrail;
import com.jie.aicode.ai.tools.*;
import com.jie.aicode.model.enums.CodeGenTypeEnum;
import com.jie.aicode.service.ChatHistoryService;
import com.jie.aicode.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class AiCodeFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;


    //存储AI的服务
    Cache<String, AiCodeService> cache = Caffeine.newBuilder()
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
    public AiCodeService getAiCodeService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        log.info("为 {} 的appId创建AI代码生成器服务", appId);
        String key = appId + ":" + codeGenTypeEnum.getValue();
        AiCodeService aiCodeService = cache.getIfPresent(key);
        if (aiCodeService == null) {
            MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                    .id(appId)
                    //todo 暂且设置为10 少成本
                    .maxMessages(10)
                    .chatMemoryStore(redisChatMemoryStore)
                    .build();
            //将聊天记录保存到内存中
            chatHistoryService.saveChatHistoryToMemory(appId, chatMemory, 10);
            aiCodeService = switch (codeGenTypeEnum) {
                case HTML, MULTI_FILE -> {
                    // 创建OpenAI流式模型
                    StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype",
                            StreamingChatModel.class);

                    yield AiServices.builder(AiCodeService.class)
                            .chatModel(chatModel)
                            .streamingChatModel(openAiStreamingChatModel)
                            .chatMemory(chatMemory)
                            .inputGuardrails(new PromptInputGuardrail())
                            .build();
                }
                case VUE, VUE_MODIFY -> {
                    // 创建深度思考流式模型 用于Vue项目
                    StreamingChatModel thinkingStreamingChatModel = SpringContextUtil.getBean("thinkingStreamingChatModel",
                            StreamingChatModel.class);

                    yield AiServices.builder(AiCodeService.class)
                            .streamingChatModel(thinkingStreamingChatModel)
                            .chatMemoryProvider(memoryId -> chatMemory)
                            .tools(
                                    new FileWriteTool(),
                                    new FileReadTool(),
                                    new FileDirReadTool(),
                                    new FileModifyTool(),
                                    new FileDeleteTool()
                            )
                            //输入内容过滤
                            .inputGuardrails(new PromptInputGuardrail())
                            //设置tool最多调用的次数
                            .maxSequentialToolsInvocations(30)
                            //工具调用错误的处理
                            .hallucinatedToolNameStrategy(request ->
                                    ToolExecutionResultMessage.toolExecutionResultMessage(request,
                                            "no tool called" + request.name()))
                            .build();
                }

            };

        }
        return aiCodeService;
    }

    /**
     * 创建AI代码生成器的服务
     *
     * @return
     */
//    @Bean
    public AiCodeService aiCodeService() {
        return this.getAiCodeService(0L, CodeGenTypeEnum.HTML);
    }

}
