package com.jie.aicode.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于redis对话记忆的配置
 */
@Configuration
@ConfigurationProperties("spring.data.redis")
@Data
public class RedisChatMemoryConfig {
    private String host;
    private int port;
    private String password;
    private int database;
    private Long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .ttl(ttl)
                .host(host)
                .port(port)
                .password(password)
                .build();
    }
}
