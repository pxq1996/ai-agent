package com.pxq.aiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Slf4j
class LoveAppDocumentLoaderTest {

    @Resource
    ResourcePatternResolver resourcePatternResolver;


    @Test
    void markdownDocumentLoaderTest() {
        LoveAppDocumentLoader loveAppDocumentLoader = new LoveAppDocumentLoader(resourcePatternResolver);
        List<Document> documentList = loveAppDocumentLoader.markdownDocumentLoader();
        log.info(documentList.toString());
    }
}