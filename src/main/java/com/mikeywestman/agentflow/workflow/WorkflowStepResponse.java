package com.mikeywestman.agentflow.workflow;

import com.mikeywestman.agentflow.agent.AgentType;

import java.util.UUID;

public record WorkflowStepResponse(
        UUID id,
        int stepOrder,
        UUID agentId,
        String agentName,
        AgentType agentType
) {}
