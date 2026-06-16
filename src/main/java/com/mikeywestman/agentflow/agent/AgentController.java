package com.mikeywestman.agentflow.agent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public List<AgentResponse> findAll() {
        return agentService.findAll();
    }

    @PostMapping
    public AgentResponse create(@Valid @RequestBody AgentRequest request) {
        return agentService.create(request);
    }
}
