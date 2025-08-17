package com.jie.aicode.monitor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MonitorContextShare {

    private static RedisTemplate<String, Object> redisTemplate;
    private static final String CONTEXT_KEY_PREFIX = "monitor:";
    private static final long DEFAULT_TIMEOUT = 20;// 默认20分钟过期



    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        MonitorContextShare.redisTemplate = redisTemplate;
    }



    /**
     * 获取当前请求的 Session ID
     * @return 当前 Session ID，如果获取不到则返回 null
     */
    private static String getCurrentSessionId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                HttpSession session = request.getSession();
                if (session != null) {
                    return session.getId();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("获取当前 Session ID 失败", e);
            return null;
        }
    }

    /**
     * 设置监控上下文（使用当前会话ID）
     */
    public static void setContext(MonitorContext context) {
        String sessionId = getCurrentSessionId();
        if (sessionId == null) {
            log.warn("无法获取当前会话ID，监控上下文设置失败");
            return;
        }
        setContext(sessionId, context);
    }

    /**
     * 设置监控上下文
     */
    public static void setContext(String sessionId, MonitorContext context) {
        try {
            String key = CONTEXT_KEY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(key, context, DEFAULT_TIMEOUT, TimeUnit.MINUTES);
            log.debug("设置监控上下文成功, sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("设置监控上下文失败, sessionId: {}", sessionId, e);
        }
    }

    /**
     * 设置监控上下文（带过期时间，使用当前会话ID）
     */
    public static void setContext(MonitorContext context, long timeout, TimeUnit timeUnit) {
        String sessionId = getCurrentSessionId();
        if (sessionId == null) {
            log.warn("无法获取当前会话ID，监控上下文设置失败");
            return;
        }
        setContext(sessionId, context, timeout, timeUnit);
    }

    /**
     * 设置监控上下文（带过期时间）
     */
    public static void setContext(String sessionId, MonitorContext context, long timeout, TimeUnit timeUnit) {
        try {
            String key = CONTEXT_KEY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(key, context, timeout, timeUnit);
            log.debug("设置监控上下文成功, sessionId: {}, timeout: {} {}", sessionId, timeout, timeUnit);
        } catch (Exception e) {
            log.error("设置监控上下文失败, sessionId: {}", sessionId, e);
        }
    }

    /**
     * 获取监控上下文（使用当前会话ID）
     */
    public static MonitorContext getContext() {
        String sessionId = getCurrentSessionId();
        if (sessionId == null) {
            log.warn("无法获取当前会话ID，监控上下文获取失败");
            return null;
        }
        return getContext(sessionId);
    }

    /**
     * 获取监控上下文
     */
    public static MonitorContext getContext(String sessionId) {
        try {
            String key = CONTEXT_KEY_PREFIX + sessionId;
            Object context = redisTemplate.opsForValue().get(key);
            if (context instanceof MonitorContext) {
                log.debug("获取监控上下文成功, sessionId: {}", sessionId);
                return (MonitorContext) context;
            }
            log.debug("监控上下文不存在, sessionId: {}", sessionId);
            return null;
        } catch (Exception e) {
            log.error("获取监控上下文失败, sessionId: {}", sessionId, e);
            return null;
        }
    }

    /**
     * 清除监控上下文（使用当前会话ID）
     */
    public static void clearContext() {
        String sessionId = getCurrentSessionId();
        if (sessionId == null) {
            log.warn("无法获取当前会话ID，监控上下文清除失败");
            return;
        }
        clearContext(sessionId);
    }

    /**
     * 清除监控上下文
     */
    public static void clearContext(String sessionId) {
        try {
            String key = CONTEXT_KEY_PREFIX + sessionId;
            redisTemplate.delete(key);
            log.debug("清除监控上下文成功, sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("清除监控上下文失败, sessionId: {}", sessionId, e);
        }
    }


}
