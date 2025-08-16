package com.jie.aicode.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存key工具类
 */
public class CacheKeyUtil {
    /**
     * 创建缓存key
     * @param o
     * @return
     */
    public static String createCacheKey(Object o) {
        if (o == null)
            return DigestUtil.md5Hex("null");
        String str = JSONUtil.toJsonStr(o);
        return DigestUtil.md5Hex(str);
    }

}
