package com.mikeywestman.agentflow.agent;

import java.util.UUID;

public record AgentResponse(
        UUID id,
        String name,
        String description,
        AgentType type,
        String systemPrompt,
        boolean enabled
) {}
