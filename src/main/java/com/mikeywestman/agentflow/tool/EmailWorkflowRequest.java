package com.mikeywestman.agentflow.tool;

import java.util.UUID;

public record EmailWorkflowRequest(
        UUID workflowId,
        String query
) {}
