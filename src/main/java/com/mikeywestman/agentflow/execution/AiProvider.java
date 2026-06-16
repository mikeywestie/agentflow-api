package com.mikeywestman.agentflow.execution;

public interface AiProvider {
    String generate(String systemPrompt, String userPrompt);
}
