package com.mikeywestman.agentflow.workflow;

import java.util.List;
import java.util.UUID;

public record WorkflowResponse(
        UUID id,
        String name,
        String description,
        boolean enabled,
        List<WorkflowStepResponse> steps
) {}
