package com.mikeywestman.agentflow.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AgentRequest(
        @NotBlank String name,
        String description,
        @NotNull AgentType type,
        @NotBlank String systemPrompt,
        boolean enabled
) {}
