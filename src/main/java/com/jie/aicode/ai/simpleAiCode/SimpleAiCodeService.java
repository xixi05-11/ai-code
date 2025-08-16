package com.jie.aicode.ai.simpleAiCode;

import com.jie.aicode.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 *
 * @author yupi
 */
public interface SimpleAiCodeService {

    /**
     * 根据用户需求智能选择代码生成类型
     * @param userPrompt 用户输入的需求描述
     * @return 推荐的代码生成类型
     */
    @SystemMessage(fromResource = "prompt/select-codeType-prompt.txt")
    CodeGenTypeEnum selectCodeType(String userPrompt);

    @SystemMessage(fromResource = "prompt/create-title-prompt.txt")
    String createTitle(String userPrompt);
}
