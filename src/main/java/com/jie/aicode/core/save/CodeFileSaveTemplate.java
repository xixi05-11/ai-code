package com.jie.aicode.core.save;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jie.aicode.exception.BusinessException;
import com.jie.aicode.exception.ErrorCode;
import com.jie.aicode.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存模板
 *
 * @param <T>
 */
public abstract class CodeFileSaveTemplate<T> {

    //文件保存根目录
    public static final String SAVE_PATH = System.getProperty("user.dir") + "/temp/code";


    /**
     * 保存并写入文件
     * @param result
     * @return
     */
    public final File save(T result) {
        validate(result);
        String path = buildFilePath();
        saveFile(path, result);
        return new File(path);
    }

    /**
     * 参数校验
     * @param result
     */
    protected void validate(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
    }

    /**
     * 构建文件的唯一路径
     */
    protected String buildFilePath() {
        String type = getType().getValue();
        String filename = StrUtil.format("{}_{}", type, IdUtil.getSnowflakeNextIdStr());
        String dirPath = SAVE_PATH + File.separator + filename;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入文件
     *
     * @param path     路径
     * @param filename 文件名
     * @param content  内容
     */
    protected static void writeFile(String path, String filename, String content) {
        String filepath = path + File.separator + filename;
        FileUtil.writeUtf8String(content, filepath);
    }

    /**
     * 获取文件类型
     * @return
     */
    protected abstract CodeGenTypeEnum getType();

    /**
     * 保存文件
     * @param path
     * @param result
     */
    protected abstract void saveFile(String path, T result);

}
