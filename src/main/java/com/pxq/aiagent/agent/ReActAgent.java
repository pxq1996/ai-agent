package com.pxq.aiagent.agent;

import com.pxq.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent{
    /**
     * 处理当前状态并且决定下一步要做什么
     * @return 是否需要执行行动（act）
     */
    public abstract Boolean think();


    /**
     *
     * @return
     */
    public abstract String act();

    @Override
    public String step(){
        try{
            boolean shouldAct = think();
            if (!shouldAct){
                // 模型思考完成，不再需要调用工具，任务结束
                this.setState(AgentState.FINISH);
                return "思考完成，无需行动";
            }
            return act();
        }catch (Exception e){
            e.printStackTrace();
            this.setState(AgentState.ERROR);
            return "行动失败:" + e.getMessage();
        }
    }
}
