package com.mikeywestman.agentflow.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Order(1)
public class GeminiAiProvider implements AiProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAiProvider(
            @Value("${app.ai.gemini-api-key:}") String apiKey,
            @Value("${app.ai.gemini-model:gemini-2.5-flash-lite}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing.");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", userPrompt))
                        )
                )
        );

        Map response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                .body(body)
                .retrieve()
                .body(Map.class);

        return extractText(response);
    }

    @Override
    public String providerName() {
        return "Gemini";
    }

    @Override
    public String modelName() {
        return model;
    }

    private String extractText(Map response) {
        List candidates = (List) response.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            return "No response returned from Gemini.";
        }

        Map candidate = (Map) candidates.get(0);
        Map content = (Map) candidate.get("content");
        List parts = (List) content.get("parts");
        Map firstPart = (Map) parts.get(0);

        return String.valueOf(firstPart.get("text"));
    }
}
