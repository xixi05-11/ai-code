package com.jie.aicode.aop;

import com.jie.aicode.annotation.Limit;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.model.entity.User;
import com.jie.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

@Aspect
@Component
public class LimitInterceptor {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param limit 权限校验注解
     */
    @Before("@annotation(limit)")
    public void authInterceptor(JoinPoint joinPoint, Limit limit) throws Throwable {
        String key = createLimitKey(joinPoint, limit);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 30分钟后过期
        rateLimiter.expire(Duration.ofMinutes(30));
        rateLimiter.trySetRate(RateType.OVERALL, limit.rate(), limit.rateInterval(),RateIntervalUnit.SECONDS);

        //获取令牌
        boolean canAccess = rateLimiter.tryAcquire(1);
        if (!canAccess) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,limit.msg());
        }
    }

    /**
     * 创建限流key
     * @param point
     * @param limit
     * @return
     */
    private String createLimitKey(JoinPoint point, Limit limit) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");
        // 添加自定义前缀
        if (!limit.key().isEmpty()) {
            keyBuilder.append(limit.key()).append(":");
        }
        // 根据限流类型生成不同的key
        switch (limit.limitType()) {
            case API:
                // 接口级别：方法名
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName());
                break;
            case USER:
                // 用户级别：用户ID
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        User loginUser = userService.getLoginUser(request);
                        keyBuilder.append("user:").append(loginUser.getId());
                    } else {
                        // 无法获取请求上下文，使用IP限流
                        keyBuilder.append("ip:").append(getClientIP());
                    }
                } catch (BusinessException e) {
                    // 未登录用户使用IP限流
                    keyBuilder.append("ip:").append(getClientIP());
                }
                break;
            case IP:
                // IP级别：客户端IP
                keyBuilder.append("ip:").append(getClientIP());
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    /**
     * 获取客户端IP
     * @return
     */
    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }


}
