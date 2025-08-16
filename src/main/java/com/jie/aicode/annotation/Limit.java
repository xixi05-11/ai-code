package com.jie.aicode.annotation;

import com.jie.aicode.model.enums.LimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {

    /**
     *  key
     * @return
     */
    String key() default "";

    /**
     * 每个时间窗口允许的请求数
     * @return
     */
    long rate() default 5L;

    /**
     * 窗口大小(单位:秒)
     * @return
     */
    long rateInterval() default 60L;

    /**
     * 限流类型
     * @return
     */
    LimitType limitType() default LimitType.USER;

    /**
     *
     * @return
     */
    String msg() default "访问过于频繁,请稍后再试";
}
