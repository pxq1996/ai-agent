# ai-agent

基于 **Spring AI Alibaba** 1.1.2.0 与 **通义千问（DashScope）** 的智能体（Agent）项目。项目采用分层继承架构，实现了基于 **ReAct（Reason + Act）** 模式的自主智能体，支持多工具调用、RAG 向量检索、MCP 客户端以及基于文件的对话记忆持久化。

---

## 核心设计要点（基于OpenManus）
- ReAct 模式：思考（think）与行动（act）严格分离，可观测、可控。
- 手动接管工具调用：通过 DashScopeChatOptions.withInternalToolExecutionEnabled(false) 禁用框架自动执行工具，改用 ToolCallingManager.executeToolCalls() 手动执行，实现每步可控。
- 自主维护上下文：消息列表由 Agent 自身维护，工具执行后从 ToolExecutionResult 回写上下文，支持多轮工具调用。
- 终止机制：通过识别 doTerminate 工具主动结束任务，避免无限循环。
- 状态机驱动：IDLE → RUNNING → FINISH / ERROR 贯穿整个生命周期。
