package com.jie.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.jie.aicode.constant.AppConstant;
import com.jie.aicode.core.AiCodeFacade;
import com.jie.aicode.core.builder.VueBuilder;
import com.jie.aicode.core.handler.StreamHandlerExecutor;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.exception.ThrowUtils;
import com.jie.aicode.mapper.AppMapper;
import com.jie.aicode.model.dto.app.AppQueryRequest;
import com.jie.aicode.model.entity.App;
import com.jie.aicode.model.entity.User;
import com.jie.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.jie.aicode.model.enums.CodeGenTypeEnum;
import com.jie.aicode.model.vo.AppVO;
import com.jie.aicode.model.vo.UserVO;
import com.jie.aicode.service.AppService;
import com.jie.aicode.service.ChatHistoryService;
import com.jie.aicode.service.ScreenshotService;
import com.jie.aicode.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 杰
 * @since 2025-07-31
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

    @Resource
    private AiCodeFacade aiCodeFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;



    /**
     * 获取脱敏APP信息
     * @param app
     * @return
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    /**
     * 批量获取脱敏APP信息
     * @param appList
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        // 判断传入的appList是否为空，如果为空则返回一个空的ArrayList
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        // 获取appList中的所有用户ID
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        // 根据用户ID批量获取用户信息，并将用户信息存入Map中
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        // 将appList中的每个app转换为AppVO，并将对应的用户信息设置到AppVO中
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 构造查询条件
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 通过提示词生成代码
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    @Override
    public Flux<String> createCode(Long appId, String message, User loginUser) {
        //参数校验
        if (appId == null || message == null || loginUser == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        //查询应用信息
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        //仅仅本人可以和自己的app交互
        if(!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无权限");
        }
        //获取app类型
        String type = app.getCodeGenType();
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(type);
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用类型错误");
        }
        //插入用户消息入库
        boolean result = chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue()
                , loginUser.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息入库失败");
        }
        //生成代码并收集入库
        Flux<String> codeStream = aiCodeFacade.createAndSaveCodeStream(message, enumByValue, appId);
        /**
         * todo: 设置VUE_MODIFY类型 暂时设置
         */
        if(enumByValue == CodeGenTypeEnum.VUE){
            app.setCodeGenType(CodeGenTypeEnum.VUE_MODIFY.getValue());
            boolean b = this.updateById(app);
            if(!b){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成代码失败");
            }
        }
        //处理流式数据
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, enumByValue);
    }

    /**
     * 部署应用
     * @param appId
     * @param loginUser
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        //参数校验
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId错误");
        }
        if(loginUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息错误");
        }
        //查询应用信息
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        //仅仅本人可以和自己的ap交互
        if(!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无权限");
        }
        //部署应用
        String deployKey = app.getDeployKey();
        if(deployKey == null || deployKey.isEmpty()){
            deployKey = RandomUtil.randomString(6);
        }
        //查找源目录
        String type = app.getCodeGenType();
        if(Objects.equals(type, "vue_modify")) {
            type = "vue";
        }
        String path = AppConstant.CODE_DIR + File.separator+ type + "_" + appId;
        //源文件
        File file = new File(path);
        if(!file.exists() || !file.isDirectory()){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"代码不存在,请重新生成");
        }
        //vue的部署
        if(type.equals(CodeGenTypeEnum.VUE.getValue())){
            boolean result = VueBuilder.buildProject(path);
            if(!result){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署失败");
            }
            File dist = new File(path, "dist");
            if(!dist.exists() || !dist.isDirectory()){
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"代码不存在,请重新生成");
            }
            file = dist;
        }
        //复制进部署目录
        String deployPath = AppConstant.CODE_DEPLOY_DIR + File.separator + deployKey;
        File deployFile = new File(deployPath);
        try {
            if (deployFile.exists()) {
                FileUtil.clean(deployFile);
            }
            // 如果是Vue项目的dist目录，则复制其中的内容而不是整个目录
            if (type.equals(CodeGenTypeEnum.VUE.getValue()) && file.getName().equals("dist")) {
                // 只复制dist目录中的内容，而不是dist目录本身
                FileUtil.copyContent(file, deployFile, true);
            } else {
                // 其他情况保持原有逻辑
                FileUtil.copyFilesFromDir(file, deployFile,true);
            }
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署失败" + e.getMessage());
        }
        //更新应用信息
        // 构建应用访问 URL
        String appDeployUrl = String.format("%s/%s/", AppConstant.DEPLOY_HOST, deployKey);
        // 异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        App newApp = new App();
        newApp.setId(appId);
        newApp.setDeployedTime(LocalDateTime.now());
        newApp.setDeployKey(deployKey);
        boolean result = this.updateById(newApp);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署失败");
        }
        return appDeployUrl;
    }



    /**
     * 异步生成应用截图并更新封面
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    /**
     * 删除app顺带删除对话历史
     * @param id
     * @return
     */
    @Override
    @Transactional
    public boolean removeById(Serializable id){
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id错误");
        }
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId错误");
        }
        //删除对话历史
        boolean result = chatHistoryService.deleteByAppId(appId);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return super.removeById(id);
    }

}
