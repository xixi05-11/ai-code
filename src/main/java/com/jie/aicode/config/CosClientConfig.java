package com.jie.aicode.config;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class CosClientConfig {

    /**
     * 域名
     */
    @Value("${cos.client.host}")
    private String host;

    /**
     * secretId
     */
    @Value("${cos.client.secretId}")
    private String secretId;

    /**
     * 密钥（注意不要泄露）
     */
    @Value("${cos.client.secretKey}")
    private String secretKey;

    /**
     * 区域
     */
    @Value("${cos.client.region}")
    private String region;

    /**
     * 桶名
     */
    @Value("${cos.client.bucket}")
    private String bucket;

    @Bean
    public COSClient cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域
        ClientConfig clientConfig = new ClientConfig(new Region(region));

        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
}
