package com.pxq.aiagent.tools;

import com.pxq.aiagent.app.LoveApp;
import com.pxq.aiagent.constant.FileConstant;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileOperationToolTest {

    @Resource
    private FileOperationTool fileOperationTool;



    @Test
    void fileReadTool() {
        String res = fileOperationTool.FileReadTool("test.txt");
        Assertions.assertNotNull(res);
    }

    @Test
    void fileWriteTool() {

        String res = fileOperationTool.FileWriteTool("test.txt","你好");
        Assertions.assertNotNull(res);
    }
}