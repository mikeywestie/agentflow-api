package com.mikeywestman.agentflow.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    public List<AgentResponse> findAll() {
        return agentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AgentResponse findById(UUID id) {
        return agentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));
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

    @Transactional
    public AgentResponse update(UUID id, AgentRequest request) {
        AgentEntity agent = agentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));

        agent.setName(request.name());
        agent.setDescription(request.description());
        agent.setType(request.type());
        agent.setSystemPrompt(request.systemPrompt());
        agent.setEnabled(request.enabled());

        return toResponse(agentRepository.save(agent));
    }

    @Transactional
    public AgentResponse setEnabled(UUID id, boolean enabled) {
        AgentEntity agent = agentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));

        agent.setEnabled(enabled);
        return toResponse(agentRepository.save(agent));
    }

    @Transactional
    public void delete(UUID id) {
        if (!agentRepository.existsById(id)) {
            throw new IllegalArgumentException("Agent not found: " + id);
        }

        agentRepository.deleteById(id);
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
