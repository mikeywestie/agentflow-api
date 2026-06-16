package com.mikeywestman.agentflow.workflow;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public List<WorkflowResponse> findAll() {
        return workflowService.findAll();
    }

    @PostMapping
    public WorkflowResponse create(@Valid @RequestBody WorkflowRequest request) {
        return workflowService.create(request);
    }
}
