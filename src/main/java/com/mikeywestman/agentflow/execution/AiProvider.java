package com.mikeywestman.agentflow.execution;

public interface AiProvider {
    AiGenerationResult generate(String systemPrompt, String userPrompt);

    String providerName();

    String modelName();
}
