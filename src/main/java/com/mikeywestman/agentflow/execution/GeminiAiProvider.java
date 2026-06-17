package com.mikeywestman.agentflow.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1)
public class GeminiAiProvider implements AiProvider {

    private final String apiKey;
    private final String model;

    public GeminiAiProvider(
            @Value("${app.ai.gemini-api-key:}") String apiKey,
            @Value("${app.ai.gemini-model:gemini-2.5-flash-lite}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing.");
        }
        throw new IllegalStateException("Gemini call needs to be re-enabled after this refactor.");
    }

    @Override
    public String providerName() {
        return "Gemini";
    }

    @Override
    public String modelName() {
        return model;
    }
}
