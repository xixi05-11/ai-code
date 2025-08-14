package com.jie.aicode.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String path = WebScreenshotUtils.saveWebPageScreenshot("https://www.baidu.com");
    }
}