package com.jie.aicode.service;

import com.jie.aicode.model.dto.app.AppQueryRequest;
import com.jie.aicode.model.entity.User;
import com.jie.aicode.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.jie.aicode.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author 杰
 * @since 2025-07-31
 */
public interface AppService extends IService<App> {

    /**
     * 获取脱敏APP信息
     * @param app
     * @return
     */
    AppVO getAppVO(App app);


    /**
     * 批量获取脱敏APP信息
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     *  构造查询条件
     * @param appQueryRequest AppQueryRequest对象
     * @return QueryWrapper对象
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 通过提示词生成代码
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> createCode(Long appId, String message , User loginUser);

    /**
     * 部署应用
     * @param appId
     * @param loginUser
     * @return
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 生成应用截图
     * @param appId
     * @param appUrl
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);
}
