package com.mikeywestman.agentflow.execution;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(3)
public class StubAiProvider implements AiProvider {

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        return "Stub response for local AgentFlow testing.";
    }

    @Override
    public String providerName() {
        return "Stub";
    }

    @Override
    public String modelName() {
        return "local-stub";
    }
}
