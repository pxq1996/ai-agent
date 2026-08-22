package com.pxq.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PxqManusTest {
    @Resource
    PxqManus pxqManus;

    @Test
    void run(){
        String prompt = """
                请把 999 写入 text.txt 文件
                """;
        String answer = pxqManus.run(prompt);
        Assertions.assertNotNull(answer);
    }
}