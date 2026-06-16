package com.mikeywestman.agentflow.execution;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record RunExecutionRequest(
        UUID workflowId,
        @NotBlank String request
) {}
