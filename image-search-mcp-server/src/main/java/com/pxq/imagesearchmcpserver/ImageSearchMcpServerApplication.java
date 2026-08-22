package com.pxq.imagesearchmcpserver;

import com.pxq.imagesearchmcpserver.tools.ImageSearchMcp;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.tools.Tool;

@SpringBootApplication
public class ImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageSearchMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider imageSearchTools(ImageSearchMcp imageSearchMcp) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchMcp)
                .build();
    }
}

