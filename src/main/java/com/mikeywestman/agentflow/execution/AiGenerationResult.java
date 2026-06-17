package com.mikeywestman.agentflow.execution;

public record AiGenerationResult(
        String output,
        String providerName,
        String modelName
) {}
