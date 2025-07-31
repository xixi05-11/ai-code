package com.jie.aicode.core.save;

import com.jie.aicode.ai.model.MultiFileCodeResult;
import com.jie.aicode.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码文件保存
 */
public class MultiFileCodeFileSave extends CodeFileSaveTemplate<MultiFileCodeResult>{
    @Override
    protected CodeGenTypeEnum getType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }


    @Override
    protected void saveFile(String path, MultiFileCodeResult result) {
        writeFile(path, "index.html", result.getHtmlCode());
        writeFile(path, "style.css", result.getCssCode());
        writeFile(path, "script.js", result.getJsCode());
    }
}
