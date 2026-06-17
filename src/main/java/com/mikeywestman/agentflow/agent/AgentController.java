package com.mikeywestman.agentflow.agent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public List<AgentResponse> findAll() {
        return agentService.findAll();
    }

    @GetMapping("/{id}")
    public AgentResponse findById(@PathVariable UUID id) {
        return agentService.findById(id);
    }

    @PostMapping
    public AgentResponse create(@Valid @RequestBody AgentRequest request) {
        return agentService.create(request);
    }

    @PutMapping("/{id}")
    public AgentResponse update(@PathVariable UUID id, @Valid @RequestBody AgentRequest request) {
        return agentService.update(id, request);
    }

    @PatchMapping("/{id}/enabled")
    public AgentResponse setEnabled(@PathVariable UUID id, @RequestParam boolean enabled) {
        return agentService.setEnabled(id, enabled);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        agentService.delete(id);
    }
}
