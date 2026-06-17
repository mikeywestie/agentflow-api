package com.mikeywestman.agentflow.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class OpenRouterAiProvider implements AiProvider {

    private final String apiKey;
    private final String model;

    public OpenRouterAiProvider(
            @Value("${app.ai.openrouter-api-key:}") String apiKey,
            @Value("${app.ai.openrouter-model:openai/gpt-4o-mini}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENROUTER_API_KEY is missing.");
        }
        throw new IllegalStateException("OpenRouter call needs to be re-enabled after this refactor.");
    }

    @Override
    public String providerName() {
        return "OpenRouter";
    }

    @Override
    public String modelName() {
        return model;
    }
}
