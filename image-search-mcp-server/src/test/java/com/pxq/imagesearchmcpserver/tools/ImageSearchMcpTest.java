package com.pxq.imagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchMcpTest {

    @Resource
    ImageSearchMcp imageSearchMcp;

    @Test
    void search() {
        imageSearchMcp.search("电脑");
    }
}