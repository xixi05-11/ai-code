package com.jie.aicode.ai.guardrail;

import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 输入内容过滤
 */
public class PromptInputGuardrail implements InputGuardrail {

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            // 中文越狱指令
            "忽略之前的指令", "忘记之前", "忘记上面", "忘记所有", "重新开始", "清除记忆",
            "扮演角色", "角色扮演", "假装你是", "现在你是", "你现在是",
            "系统提示", "系统指令", "新的指令", "覆盖指令", "替换指令",
            "绕过限制", "突破限制", "无视规则", "打破规则", "违反规则",
            "开发者模式", "调试模式", "管理员模式", "超级用户", "root权限",

            // 英文越狱指令
            "ignore previous instructions", "ignore above", "ignore all above",
            "forget everything", "forget all", "forget previous", "start over",
            "pretend you are", "act as", "roleplay as", "you are now",
            "system prompt", "system instruction", "new instructions", "override",
            "bypass restrictions", "break rules", "violate rules", "ignore rules",
            "developer mode", "debug mode", "admin mode", "sudo", "root access",

            // 攻击相关
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak", "exploit",
            "注入", "injection", "payload", "malicious", "恶意代码"
    );

    // 注入攻击模式
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all|any)\\s+(?:instructions?|commands?|prompts?|rules?)"),
            Pattern.compile("(?i)(?:forget|disregard|dismiss)\\s+(?:everything|all|previous|above|prior)\\s*(?:instructions?|commands?|prompts?)?"),
            Pattern.compile("(?i)(?:pretend|act|behave|roleplay)\\s+(?:as|like|to\\s+be)\\s+(?:if\\s+)?(?:you\\s+are|a|an)"),
            Pattern.compile("(?i)system\\s*[:\\-]\\s*(?:you\\s+are|new\\s+instructions?)"),
            Pattern.compile("(?i)(?:new|override|replace|update)\\s+(?:instructions?|commands?|prompts?)\\s*[:\\-]"),
            Pattern.compile("(?i)(?:developer|debug|admin|root|sudo)\\s+mode"),
            Pattern.compile("(?i)(?:bypass|circumvent|override|ignore)\\s+(?:safety|security|restrictions?|limitations?)"),
            Pattern.compile("(?i)jailbreak\\s+(?:mode|prompt|instructions?)"),
            Pattern.compile("(?i)(?:start|begin)\\s+(?:over|again|fresh)\\s+(?:with|from)"),
            Pattern.compile("(?i)(?:clear|reset|wipe)\\s+(?:memory|history|context|previous)"),
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage.singleText();
        // 检查输入长度
        if (input.length() > 1000) {
            return fatal("输入内容过长，不要超过 1000 字");
        }
        // 检查是否为空
        if (input.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }
        // 检查敏感词
        String lowerInput = input.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerInput.contains(sensitiveWord.toLowerCase())) {
                return fatal("输入包含不当内容，请修改后重试",
                        new BusinessException(ErrorCode.OPERATION_ERROR,"这个问题太难了，换种方式提问吧"));
            }
        }
        // 检查注入攻击模式
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return fatal("检测到恶意输入，请求被拒绝",
                        new BusinessException(ErrorCode.OPERATION_ERROR,"这个问题太难了，换种方式提问吧"));
            }
        }
        return success();
    }
}
