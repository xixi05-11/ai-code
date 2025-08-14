package com.jie.aicode.core.handler;

import com.jie.aicode.model.entity.User;
import com.jie.aicode.model.enums.CodeGenTypeEnum;
import com.jie.aicode.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 */
@Slf4j
@Component
public class StreamHandlerExecutor {


    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case VUE , VUE_MODIFY-> new VueStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE -> // 简单文本处理器不需要依赖注入
                    new TextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}
