package com.jie.aicode.core;

import com.jie.aicode.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AiCodeFacadeTest {

    @Resource
    private AiCodeFacade aiCodeFacade;
    @Test
    void createAndSaveCode() {
        File file = aiCodeFacade.createAndSaveCode("生成一个登陆界面 不超过20行",
                CodeGenTypeEnum.MULTI_FILE);
        assertNotNull(file);
    }

    @Test
    void createAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeFacade.createAndSaveCodeStream("任务记录网站,少于30行", CodeGenTypeEnum.MULTI_FILE);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }


}