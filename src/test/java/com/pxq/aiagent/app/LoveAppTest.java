package com.pxq.aiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class LoveAppTest {

    @Resource
    private LoveApp loveApp;
    @Test
    void testChat() {
        String uuid = UUID.randomUUID().toString();
        String message = "你好，我是tom";
        String answer = loveApp.doChat(message,uuid);

        message = "今天赚了1w";
        answer = loveApp.doChat(message,uuid);

        message = "你知道我是谁吗？今天赚了多少钱";
        answer = loveApp.doChat(message,uuid);
    }

    @Test
    void doChatWithReport() {
        String uuid = UUID.randomUUID().toString();
        String message = "我想让我的另一半更加爱我但我不知道怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message,uuid);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String uuid = UUID.randomUUID().toString();
        String message = "我想让我的另一半更加爱我但我不知道怎么做";
        String s = loveApp.doChatWithRag(uuid, message);

        Assertions.assertNotNull(s);
    }


    @Test
    void doChatWithMcp() {
        String uuid = UUID.randomUUID().toString();
        String message = "帮我搜索一张电脑的图片";
        String s = loveApp.doChatWithMcp(uuid, message);

        Assertions.assertNotNull(s);
    }
}