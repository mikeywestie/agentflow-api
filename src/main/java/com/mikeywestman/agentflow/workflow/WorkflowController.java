package com.mikeywestman.agentflow.workflow;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public List<WorkflowResponse> findAll() {
        return workflowService.findAll();
    }

    @GetMapping("/{id}")
    public WorkflowResponse findById(@PathVariable UUID id) {
        return workflowService.findById(id);
    }

    @PostMapping
    public WorkflowResponse create(@Valid @RequestBody WorkflowRequest request) {
        return workflowService.create(request);
    }

    @PutMapping("/{id}")
    public WorkflowResponse update(@PathVariable UUID id, @Valid @RequestBody WorkflowRequest request) {
        return workflowService.update(id, request);
    }

    @PatchMapping("/{id}/enabled")
    public WorkflowResponse setEnabled(@PathVariable UUID id, @RequestParam boolean enabled) {
        return workflowService.setEnabled(id, enabled);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        workflowService.delete(id);
    }
}
