package com.jie.aicode.core;

import cn.hutool.ai.AIServiceFactory;
import com.jie.aicode.ai.AiCodeFactory;
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
    private AiCodeFactory aiCodeFactory;



    /**
     * 流式 生成代码并保存
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId
     * @return
     */
    public Flux<String> createAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成类型");
        }
        return SaveCodeStream(userMessage, codeGenTypeEnum, appId);
    }

    /**
     * 生成代码并保存
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId
     * @return
     */
    public File createAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择代码生成类型");
        }
        // 获取代码生成服务
        AiCodeService aiCodeService = aiCodeFactory.getAiCodeService(appId);
        return CodeFileSaveExecutor.save(aiCodeService.generateHtmlCode(userMessage), codeGenTypeEnum, appId);
    }


    /**
     * 流式  生成并保存代码
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId
     * @return
     */
    private Flux<String> SaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        StringBuilder sbCode = new StringBuilder();
        // 获取代码生成服务
        AiCodeService aiCodeService = aiCodeFactory.getAiCodeService(appId);

        Flux<String> flux = switch (codeGenTypeEnum) {
            case HTML -> aiCodeService.generateHtmlCodeStream(userMessage);
            case MULTI_FILE -> aiCodeService.generateMultiFileCodeStream(userMessage);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂时不支持的代码生成类型");
        };
        return flux
                // 拼接输出
                .doOnNext(sbCode::append)
                .doOnComplete(() -> {
                    try {
                        String code = sbCode.toString();
                        Object result = CodeParseExecutor.parse(code, codeGenTypeEnum);
                        File file = CodeFileSaveExecutor.save(result, codeGenTypeEnum, appId);
                        log.info("生成代码成功,文件路径:{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存代码失败,{}", e.getMessage());
                    }
                });
    }


}
