package com.jie.aicode.core;

import cn.hutool.json.JSONUtil;
import com.jie.aicode.ai.AiCodeFactory;
import com.jie.aicode.ai.AiCodeService;
import com.jie.aicode.ai.model.message.AiResponseMessage;
import com.jie.aicode.ai.model.message.ToolExecutedMessage;
import com.jie.aicode.ai.model.message.ToolRequestMessage;
import com.jie.aicode.core.parse.CodeParseExecutor;
import com.jie.aicode.core.save.CodeFileSaveExecutor;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        // 根据 appId 获取相应的 AI 服务实例
        AiCodeService aiCodeService = aiCodeFactory.getAiCodeService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE -> {
                TokenStream tokenStream = aiCodeService.generateVueCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);
            }
            case VUE_MODIFY -> {
                TokenStream tokenStream = aiCodeService.modifyVueCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
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
        AiCodeService aiCodeService = aiCodeFactory.getAiCodeService(appId, codeGenTypeEnum);
        return CodeFileSaveExecutor.save(aiCodeService.generateHtmlCode(userMessage), codeGenTypeEnum, appId);
    }


    /**
     * 流式代码处理
     * @param codeStream
     * @param codeGenType
     * @param appId
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        // 字符串拼接器，用于当流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后，保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParseExecutor.parse(completeCode, codeGenType);
                // 使用执行器保存代码
                File saveDir = CodeFileSaveExecutor.save(parsedResult, codeGenType, appId);
                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }



    /**
     * 流式处理TokenStream 转化为Flux
     *
     * @param tokenStream
     * @return
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, tool)->{
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(tool);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


}
