package com.mikeywestman.agentflow.execution;

import com.mikeywestman.agentflow.agent.AgentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgentRunResponse(
        UUID id,
        String agentName,
        AgentType agentType,
        String input,
        String output,
        ExecutionStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}
