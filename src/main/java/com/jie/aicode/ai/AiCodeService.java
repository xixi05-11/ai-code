package com.jie.aicode.ai;

import com.jie.aicode.ai.model.HtmlCodeResult;
import com.jie.aicode.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeService {

    /**
     * 生成 HTML 代码
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/html-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/multi-file-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 流式 生成 HTML 代码
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/html-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 流失 生成多文件代码
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/multi-file-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成 vue代码
     * @param appId
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/vue-prompt.txt")
    TokenStream generateVueCodeStream(@MemoryId long appId, @UserMessage String userMessage);


    /**
     * 修改 vue代码
     * @param appId
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/vue-modify-prompt.txt")
    TokenStream modifyVueCodeStream(@MemoryId long appId, @UserMessage String userMessage);


}
