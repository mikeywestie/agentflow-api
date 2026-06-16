package com.mikeywestman.agentflow.execution;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final AgentOrchestratorService orchestratorService;

    @PostMapping("/run")
    public ExecutionResponse run(@Valid @RequestBody RunExecutionRequest request) {
        return orchestratorService.run(request);
    }

    @GetMapping
    public List<ExecutionResponse> findLatest() {
        return orchestratorService.findLatest();
    }

    @GetMapping("/{id}")
    public ExecutionResponse findById(@PathVariable UUID id) {
        return orchestratorService.findById(id);
    }
}
