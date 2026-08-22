package com.pxq.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.pxq.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现think和act方法，可以用作创建实例的父类
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent{
    // 可以用的工具
    private ToolCallback[] availableTools;

    // 工具的响应内容
    private ChatResponse toolCallChatResponse;

    // 管理工具
    private final ToolCallingManager toolCallingManager;

    // 禁用框架自带的工具调用，手动维护工具
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] toolCallback){
        super();
        this.availableTools = toolCallback;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();

    }

    /**
     * 处理当前任务并执行下一步
     * @return
     */
    @Override
    public Boolean think() {
        // 校验提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        // 调用llm，获取工具调用结果
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatRespon = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();

            // 记录响应用于act
            this.toolCallChatResponse = chatRespon;
            // 解析工具调用结果，获取要调用的工具
            AssistantMessage assistantMessage = chatRespon.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallsList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了：" + toolCallsList.size() + "个工具使用");
            String toolCallInfo = toolCallsList.stream()
                    .map(toolCall -> String.format("工具名称：" + toolCall.name()) + "工具参数：" + toolCall.arguments())
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 如果不需要调用工具则返回false
            if (toolCallInfo.isEmpty()){
                // 添加助手消息（只有不需要调用工具时才需要）
                getMessageList().add(assistantMessage);
                return false;
            }else {
                // 调用工具时无需记录，因为回自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理遇到了问题" + e.getMessage()));
            return false;
        }
    }


    /**
     * 执行工具调用并处理结果
     * @return 返回执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()){
            return "不需要调用工具！";
        }
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage)CollUtil.getLast(toolExecutionResult.conversationHistory());

        // 判断是否调用了终止工具
        boolean terminateToolCall = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));

        if (terminateToolCall){
            setState(AgentState.FINISH);
        }

        String results = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具：" + toolResponse.name() + "返回的结果" + toolResponse.responseData())
                .collect(Collectors.joining("\n"));


        return results;
    }
}
