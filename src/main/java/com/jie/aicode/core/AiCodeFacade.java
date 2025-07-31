package com.jie.aicode.core;

import com.jie.aicode.ai.AiCodeService;
import com.jie.aicode.core.parse.CodeParseExecutor;
import com.jie.aicode.core.save.CodeFileSaveExecutor;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 整合AI代码生成与保存
 */
@Component
@Slf4j
public class AiCodeFacade {

    @Resource
    private AiCodeService aiCodeService;


    /**
     * 流式 生成代码并保存
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> createAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成类型");
        }
        return SaveCodeStream(userMessage, codeGenTypeEnum);
    }

    /**
     * 生成代码并保存
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File createAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成类型");
        }
        return CodeFileSaveExecutor.save(aiCodeService.generateHtmlCode(userMessage), codeGenTypeEnum);
    }


    /**
     * 流式  生成并保存代码
     * @param userMessage
     * @return
     */
    private Flux<String> SaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        StringBuilder sbCode = new StringBuilder();
        return aiCodeService.generateHtmlCodeStream(userMessage)
                // 拼接输出
                .doOnNext(sbCode::append)
                .doOnComplete(() -> {
                    try {
                        String code = sbCode.toString();
                        Object result = CodeParseExecutor.parse(code, codeGenTypeEnum);
                        File file = CodeFileSaveExecutor.save(result, codeGenTypeEnum);
                        log.info("生成HTML代码成功,文件路径:{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存代码失败,{}", e.getMessage());
                    }
                });
    }


}
