package com.mikeywestman.agentflow.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Primary
@Service
public class GeminiAiProvider implements AiProvider {

    private final RestClient restClient;
    private final String apiKey;

    public GeminiAiProvider(@Value("${gemini.api-key:}") String apiKey) {
        this.apiKey = apiKey;
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
                        "parts", List.of(
                                Map.of("text", systemPrompt)
                        )
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", userPrompt)
                                )
                        )
                )
        );

        Map response = restClient.post()
                .uri("/v1beta/models/gemini-2.5-flash-lite:generateContent?key={apiKey}", apiKey)
                .body(body)
                .retrieve()
                .body(Map.class);

        return extractText(response);
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