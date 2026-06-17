package com.mikeywestman.agentflow.tool;

import com.mikeywestman.agentflow.execution.ExecutionResponse;

import java.util.List;

public record EmailWorkflowResponse(
        String query,
        int matchedMessageCount,
        ToolAccessLevel accessLevel,
        String safetyNote,
        List<EmailToolMessage> messages,
        ExecutionResponse execution
) {}
