package com.mikeywestman.agentflow.execution;

import org.springframework.stereotype.Service;

@Service
public class StubAiProvider implements AiProvider {

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        return "[Stub AI Output]\n\nSystem prompt:\n" + systemPrompt + "\n\nUser input:\n" + userPrompt + "\n\nNext: Replace StubAiProvider with GeminiAiProvider in Week 2.";
    }
}
