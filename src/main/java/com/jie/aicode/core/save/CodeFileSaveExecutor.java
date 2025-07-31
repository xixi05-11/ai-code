package com.jie.aicode.core.save;

import com.jie.aicode.ai.model.HtmlCodeResult;
import com.jie.aicode.ai.model.MultiFileCodeResult;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 */
public class CodeFileSaveExecutor {
    public static final HtmlCodeFileSave htmlCodeFileSave = new HtmlCodeFileSave();
    public static final MultiFileCodeFileSave multiFileCodeFileSave = new MultiFileCodeFileSave();

    /**
     * 保存并写入文件
     * @param result
     * @param codeGenTypeEnum
     * @return
     */
    public static File save(Object result, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML:
                yield htmlCodeFileSave.save((HtmlCodeResult) result);
            case MULTI_FILE:
                yield multiFileCodeFileSave.save((MultiFileCodeResult) result);
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂时不支持的代码生成类型");
        };
    }
}
