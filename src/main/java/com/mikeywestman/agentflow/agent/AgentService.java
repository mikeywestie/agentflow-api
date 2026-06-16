package com.mikeywestman.agentflow.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    public List<AgentResponse> findAll() {
        return agentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AgentResponse create(AgentRequest request) {
        AgentEntity agent = AgentEntity.builder()
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .systemPrompt(request.systemPrompt())
                .enabled(request.enabled())
                .build();

        return toResponse(agentRepository.save(agent));
    }

    private AgentResponse toResponse(AgentEntity agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getType(),
                agent.getSystemPrompt(),
                agent.isEnabled()
        );
    }
}
