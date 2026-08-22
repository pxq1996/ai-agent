package com.pxq.aiagent.app;

import com.pxq.aiagent.advisor.MyLoggerAdvisor;
import com.pxq.aiagent.advisor.ReReadingAdvisor;
import com.pxq.aiagent.chatmemory.FileBasedChatMemory;
import com.pxq.aiagent.rag.LoveAppVectorStoreConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoveApp {
    public final ChatClient chatClient;

    @Resource
    VectorStore loveAppVectorStore;


    private static final String SYSTEM_PROMPT="扮演深耕恋爱心理领域的专家。" +
            "开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；" +
            "已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";
    private String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";



    public LoveApp(ChatModel dashscopeChatModel){
        String fileDir = System.getProperty("user.dir")+"/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor())
                        // 传输两次用户的上下文
                        // new ReReadingAdvisor())
                .build();

    }

    public String doChat(String message, String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .call()
                .chatResponse();
        String content =  chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions){}

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    public String doChatWithRag(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .advisors(new MyLoggerAdvisor())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1))
                .advisors(buildRagAdvisor())
                .call().chatResponse();
        String text = response.getResult().getOutput().getText();
        log.info("loveReport: {}", text);
        return text;
    }

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .advisors(new MyLoggerAdvisor())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1))
                .advisors(buildRagAdvisor())
                .call().chatResponse();
        String text = response.getResult().getOutput().getText();
        log.info("loveReport: {}", text);
        return text;
    }

    /**
     * 构建 RAG 检索增强 Advisor（Spring AI 1.1.x 使用 RetrievalAugmentationAdvisor
     * 替代已被移除的 QuestionAnswerAdvisor）。
     */
    private RetrievalAugmentationAdvisor buildRagAdvisor() {
        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(loveAppVectorStore)
                .topK(1)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();
    }

}
