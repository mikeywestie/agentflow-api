package com.mikeywestman.agentflow.execution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExecutionResponse(
        UUID id,
        UUID workflowId,
        String workflowName,
        String request,
        String finalOutput,
        ExecutionStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        List<AgentRunResponse> agentRuns
) {}
