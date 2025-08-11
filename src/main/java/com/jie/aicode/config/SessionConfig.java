package com.jie.aicode.config;

import org.springframework.boot.autoconfigure.session.DefaultCookieSerializerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;

// session配置

/**
 * 解决尝试通过 Set-Cookie 标头设置 Cookie 的操作被禁止了，
 * 因为此标头具有 ‘SameSite=Lax’ 属性但来自一个跨网站响应，而该响应并不是对顶级导航操作的响应
 */
@Configuration
@EnableSpringHttpSession
public class SessionConfig {

 
    @Bean
    DefaultCookieSerializerCustomizer cookieSerializerCustomizer() {
        return new DefaultCookieSerializerCustomizer() {
            @Override
            public void customize(DefaultCookieSerializer cookieSerializer) {
                cookieSerializer.setSameSite("None"); // 设置cookie的SameSite属性为None，否则跨域set-cookie会被chrome浏览器阻拦
                cookieSerializer.setUseSecureCookie(true); // sameSite为None时，useSecureCookie必须为true
            }
        };
    }
}