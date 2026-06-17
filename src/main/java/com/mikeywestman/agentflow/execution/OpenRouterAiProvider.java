package com.mikeywestman.agentflow.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Order(2)
public class OpenRouterAiProvider implements AiProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenRouterAiProvider(
            @Value("${app.ai.openrouter-api-key:}") String apiKey,
            @Value("${app.ai.openrouter-model:openai/gpt-4o-mini}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://openrouter.ai")
                .build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENROUTER_API_KEY is missing.");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        Map response = restClient.post()
                .uri("/api/v1/chat/completions")
                .header("Authorization", "Bearer ".concat(apiKey))
                .header("HTTP-Referer", "http://localhost:5173")
                .header("X-Title", "AgentFlow")
                .body(body)
                .retrieve()
                .body(Map.class);

        return extractText(response);
    }

    @Override
    public String providerName() {
        return "OpenRouter";
    }

    @Override
    public String modelName() {
        return model;
    }

    private String extractText(Map response) {
        List choices = (List) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "No response returned from OpenRouter.";
        }

        Map choice = (Map) choices.get(0);
        Map message = (Map) choice.get("message");

        return String.valueOf(message.get("content"));
    }
}
