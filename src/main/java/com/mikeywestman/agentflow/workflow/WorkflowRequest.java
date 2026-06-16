package com.mikeywestman.agentflow.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record WorkflowRequest(
        @NotBlank String name,
        String description,
        boolean enabled,
        @NotEmpty List<UUID> agentIds
) {}
