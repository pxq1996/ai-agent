package com.pxq.aiagent.agent;

import com.pxq.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;


import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public abstract class BaseAgent {
    // agent 名字
    private String name;
    // 模型
    private ChatClient chatClient;
    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;
    // memory （需要自主维护上下文）
    private List<Message> messageList = new ArrayList<>();
    // 执行次数控制
    private int maxStep = 10;
    private int currentStep = 0;

    public String run(String userPrompt){
        if (this.state != AgentState.IDLE){
            throw new RuntimeException("当前状态不能运行：" + this.state);
        }

        if(userPrompt==null){
            throw new RuntimeException("无用户提示词无法运行...");
        }

        // 更改agent状态
        this.state = AgentState.RUNNING;

        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));

        // 保存结果列表
        List<String> results = new ArrayList<>();

        try {
            for (int i = 0; i < maxStep && this.state != AgentState.FINISH; i++){
                int stepNumber = i + 1;
                this.currentStep = stepNumber;
                log.info("执行step:" + stepNumber + "/" + maxStep);

                // 执行单步
                String stepResult = step();
                String result = "第" + stepNumber + "步响应结果:" + stepResult;
                results.add(result);
            }

            if (this.currentStep>=maxStep){
                this.state = AgentState.FINISH;
                results.add("agent运行达到最大次数：" + maxStep);
            }
            return String.join("\n",results);
        }catch (Exception e){
            this.state = AgentState.ERROR;
            return "执行错误：" + e.getMessage();
        }


    }

    /**
     * 单次执行步骤
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void leanUp(){};
}
