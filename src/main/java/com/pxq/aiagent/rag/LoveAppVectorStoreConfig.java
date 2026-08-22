package com.pxq.aiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStoreContent;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;


@Configuration
@Slf4j
public class LoveAppVectorStoreConfig {
    @Resource
    LoveAppDocumentLoader loveAppDocumentLoader;


    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel){
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> documentList = loveAppDocumentLoader.markdownDocumentLoader();
        vectorStore.doAdd(documentList);
        return vectorStore;
    }

}
