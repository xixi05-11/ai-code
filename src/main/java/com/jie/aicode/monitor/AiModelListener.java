package com.jie.aicode.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class AiModelListener implements ChatModelListener {

    @Resource
    private AiModelMetricsCollector metricsCollector;

    private static final String START_TIME = "start_time";
    private static final String CONTEXT = "context";
    private static final String START_STATUS = "start";
    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // 记录开始时间
        requestContext.attributes().put(START_TIME, Instant.now());
        MonitorContext context = MonitorContextShare.getContext();
        String userId = context.getUserId();
        String appId = context.getAppId();
        // 设置监控上下文
        requestContext.attributes().put(CONTEXT, context);

        ChatRequest chatRequest = requestContext.chatRequest();
        ChatRequestParameters parameters = chatRequest.parameters();
        String modelName = parameters.modelName();
        metricsCollector.recordRequest(userId, appId, modelName, START_STATUS);


    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // 记录响应时间
        Instant startTime = (Instant) responseContext.attributes().get(START_TIME);
        MonitorContext context = (MonitorContext) responseContext.attributes().get(CONTEXT);
        String userId = context.getUserId();
        String appId = context.getAppId();
        ChatResponse chatResponse = responseContext.chatResponse();
        String modelName = chatResponse.modelName();
        TokenUsage tokenUsage = chatResponse.metadata().tokenUsage();
        if (tokenUsage != null) {
            metricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            metricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            metricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
        }
        // 记录响应成功的请求
        metricsCollector.recordRequest(userId, appId, modelName, SUCCESS_STATUS);
        // 记录响应时间
        metricsCollector.recordResponseTime(userId, appId, modelName, Duration.between(startTime, Instant.now()));
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        MonitorContext context = MonitorContextShare.getContext();
        String userId = context.getUserId();
        String appId = context.getAppId();
        String modelName = errorContext.chatRequest().modelName();

        // 记录响应失败的请求
        metricsCollector.recordRequest(userId, appId, modelName, ERROR_STATUS);
        // 记录错误信息
        metricsCollector.recordError(userId, appId, modelName, errorContext.error().getMessage());
        Instant startTime = (Instant)errorContext.attributes().get(START_TIME);

        // 响应时间
        metricsCollector.recordResponseTime(userId, appId, modelName, Duration.between(startTime, Instant.now()));

    }
}
