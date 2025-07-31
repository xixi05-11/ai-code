package com.jie.aicode.core.save;

import com.jie.aicode.ai.model.HtmlCodeResult;
import com.jie.aicode.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存
 */
public class HtmlCodeFileSave extends CodeFileSaveTemplate<HtmlCodeResult>{
    @Override
    protected CodeGenTypeEnum getType() {
        return CodeGenTypeEnum.HTML;
    }


    @Override
    protected void saveFile(String path, HtmlCodeResult result) {
        writeFile(path,"index.html",result.getHtmlCode());
    }
}
