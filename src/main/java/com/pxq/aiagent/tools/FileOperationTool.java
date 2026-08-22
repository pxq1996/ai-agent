package com.pxq.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.pxq.aiagent.constant.FileConstant;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class FileOperationTool {

    private String dir_path = FileConstant.FILE_PATH;

    @Tool(description = "这是一个文件读取工具")
    public String FileReadTool(@ToolParam(description = "传入的文件地址") String fileName){
        String filePath = dir_path + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        }catch (Exception e){
            return "read error " + e.getMessage();
        }
    }

    @Tool(description = "这是一个文件写入工具")
    public String FileWriteTool(@ToolParam(description = "写入的文件地址") String fileName,
                                @ToolParam(description = "写入文件中的内容") String content){
        String filePath = dir_path + "/" + fileName;
        try {
            FileUtil.file(filePath);
            FileUtil.writeUtf8String(content,filePath);
            return "写入成功！"+filePath;
        }catch (Exception e){
            return "写入失败！ error " + e.getMessage();
        }
    }
}
