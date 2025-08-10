package com.jie.aicode.controller;

import com.jie.aicode.annotation.AuthCheck;
import com.jie.aicode.common.BaseResponse;
import com.jie.aicode.common.ResultUtils;
import com.jie.aicode.config.CosClientConfig;
import com.jie.aicode.constant.UserConstant;
import com.jie.aicode.cos.CosManager;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.model.entity.User;
import com.jie.aicode.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
@RestController
public class FileController {

    private final CosManager cosManager;

    private final UserService userService;

    private final CosClientConfig cosClientConfig;

    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/user/upload")
    public BaseResponse<String> UploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           HttpServletRequest request) {
        // 文件目录
        User loginUser = userService.getLoginUser(request);
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s", loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            //存入数据库
            loginUser.setUserAvatar(cosClientConfig.getHost() + File.separator + filepath);
            boolean result = userService.updateById(loginUser);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
            }
            // 返回可访问地址
            return ResultUtils.success(cosClientConfig.getHost() + File.separator + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }

        }
    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }


}
