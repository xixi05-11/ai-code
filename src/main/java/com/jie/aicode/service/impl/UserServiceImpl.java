package com.jie.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jie.aicode.constant.UserConstant;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.mapper.UserMapper;
import com.jie.aicode.model.dto.user.UserChangePwdRequest;
import com.jie.aicode.model.dto.user.UserQueryRequest;
import com.jie.aicode.model.entity.User;
import com.jie.aicode.model.enums.UserRoleEnum;
import com.jie.aicode.model.vo.LoginUserVO;
import com.jie.aicode.model.vo.UserVO;
import com.jie.aicode.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 服务层实现。
 *
 * @author 杰
 * @since 2025-07-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long register(String userAccount, String userPassword, String checkPassword) {
        //参数校验
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //查询用户是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(User::getUserAccount, userAccount);
        User queryUser = this.mapper.selectOneByQuery(queryWrapper);
        if (queryUser != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已存在");
        }

        //添加用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryption(userPassword));
        user.setUserName("默认昵称");
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setEditTime(new Date());
        user.setCreateTime(new Date());
        boolean save = this.save(user);
        if (!save){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"添加用户失败");
        }
        return user.getId();
    }

    /**
     *  用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //参数校验
        if (StrUtil.isEmpty(userAccount) || StrUtil.isEmpty(userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码为空");
        }
        //判断账号密码是否正确
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryption(userPassword));
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码错误");
        }
       //保存信息到session中
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 返回脱敏的用户信息
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object o = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) o;
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        user = this.getById(user.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户不存在");
        }
        return user;
    }

    /**
     * 用户登出
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object o = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) o;
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取脱敏用户信息
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏用户信息列表
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 重置密码
     * @param userId
     * @return
     */
    @Override
    public boolean resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        user.setUserPassword(encryption(UserConstant.DEFAULT_PASSWORD));
        return this.updateById(user);
    }

    /**
     * 修改密码
     * @param userChangePwdRequest
     * @param request
     * @return
     */
    @Override
    public boolean changeUserPwd(UserChangePwdRequest userChangePwdRequest,HttpServletRequest request) {
        String oldPassword = userChangePwdRequest.getOldPassword();
        String userPassword = userChangePwdRequest.getUserPassword();
        String checkPassword = userChangePwdRequest.getCheckPassword();
        //参数校验
        User user = this.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        if (!encryption(oldPassword).equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }
        if(userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }
        //修改密码
        user.setUserPassword(encryption(userPassword));
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改密码失败");
        }
        //登出
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 加密密码
     * @param password
     * @return
     */
    @Override
    public String encryption(String password){
        final String salt = "jie";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }


}
