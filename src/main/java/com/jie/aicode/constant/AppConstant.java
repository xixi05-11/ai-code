package com.jie.aicode.constant;

public interface AppConstant {

    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 应用生成目录
     */
    String CODE_DIR = System.getProperty("user.dir") + "/temp/code";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_DIR = System.getProperty("user.dir") + "/temp/deploy";

    /**
     * 应用部署域名
     */
    String DEPLOY_HOST = "http://localhost";

}
