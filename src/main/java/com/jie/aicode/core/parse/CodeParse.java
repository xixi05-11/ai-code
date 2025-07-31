package com.jie.aicode.core.parse;

/**
 * 代码解析
 */
public interface CodeParse<T> {

    /**
     * 解析原始代码
     * @param code
     * @return
     */
    T parse(String code);
}
