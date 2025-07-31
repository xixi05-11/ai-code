package com.jie.aicode.core.parse;

import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 */
public class CodeParseExecutor {

    public static final HtmlCodeParse htmlCodeParse = new HtmlCodeParse();
    public static final MultiFileCodeParse multiFileCodeParse = new MultiFileCodeParse();

    /**
     * 解析代码
     * @param code
     * @param codeGenTypeEnum
     * @return
     */
    public static Object parse(String code, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML:
                yield htmlCodeParse.parse(code);
            case MULTI_FILE:
                yield multiFileCodeParse.parse(code);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂时不支持的代码生成类型");
        };
    }
}
