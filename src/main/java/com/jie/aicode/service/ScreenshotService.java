package com.jie.aicode.service;

public interface ScreenshotService {
    /**
     * 生成并上传截图
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);
}
