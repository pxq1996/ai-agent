package com.pxq.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LoveAppDocumentLoader {

    // 定义一个文档解析器
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver){
        this.resourcePatternResolver = resourcePatternResolver;
    }

    // 加载文档
    public List<Document> markdownDocumentLoader(){
        List<Document> documentList = new ArrayList<>();
        try {
            // 获取文件加入到数组
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for(Resource resource:resources){
                String fileName = resource.getFilename();
                MarkdownDocumentReaderConfig config = null;
                if (fileName != null) {
                    config = MarkdownDocumentReaderConfig.builder()
                            .withIncludeCodeBlock(false)
                            .withHorizontalRuleCreateDocument(false)
                            .withAdditionalMetadata("filename",fileName)
                            .build();
                }
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                documentList.addAll(markdownDocumentReader.get());

            }
        } catch (IOException e) {
            log.info("获取文件失败");
        }
        return documentList;
    }
}
