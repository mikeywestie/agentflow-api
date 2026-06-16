package com.mikeywestman.agentflow.execution;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
@RequiredArgsConstructor
public class CompositeAiProvider implements AiProvider {

    private final GeminiAiProvider geminiAiProvider;
    private final OpenRouterAiProvider openRouterAiProvider;

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        RuntimeException firstError;

        try {
            return geminiAiProvider.generate(systemPrompt, userPrompt);
        } catch (RuntimeException ex) {
            firstError = ex;
            System.out.println("Gemini call did not complete. Trying OpenRouter.");
        }

        try {
            String result = openRouterAiProvider.generate(systemPrompt, userPrompt);
            return "[Provider used: OpenRouter]\n\n" + result;
        } catch (RuntimeException secondError) {
            throw new RuntimeException(
                    "All configured AI providers failed. First error: " + summarize(firstError.getMessage())
                            + " Second error: " + summarize(secondError.getMessage())
            );
        }
    }

    private String summarize(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown error";
        }

        return message.length() > 500
                ? message.substring(0, 500) + "..."
                : message;
    }
}
