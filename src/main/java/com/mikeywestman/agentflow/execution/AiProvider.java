package com.mikeywestman.agentflow.execution;

public interface AiProvider {
    String generate(String systemPrompt, String userPrompt);

    default String providerName() {
        return getClass().getSimpleName();
    }

    default String modelName() {
        return "unknown";
    }
}
