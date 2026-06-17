package com.mikeywestman.agentflow.execution;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}/export/json")
    public ExecutionResponse exportJson(@PathVariable UUID id) {
        return orchestratorService.findById(id);
    }

    @GetMapping(value = "/{id}/export/markdown", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public ResponseEntity<String> exportMarkdown(@PathVariable UUID id) {
        ExecutionResponse execution = orchestratorService.findById(id);
        String markdown = toMarkdown(execution);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=agentflow-execution-" + id + ".md")
                .contentType(MediaType.TEXT_MARKDOWN)
                .body(markdown);
    }

    private String toMarkdown(ExecutionResponse execution) {
        StringBuilder builder = new StringBuilder();
        builder.append("# AgentFlow Execution\n\n");
        builder.append("## Summary\n\n");
        builder.append("- Execution ID: ").append(execution.id()).append("\n");
        builder.append("- Workflow: ").append(nullSafe(execution.workflowName())).append("\n");
        builder.append("- Status: ").append(execution.status()).append("\n");
        builder.append("- Started: ").append(execution.startedAt()).append("\n");
        builder.append("- Completed: ").append(execution.completedAt()).append("\n\n");

        builder.append("## Original Request\n\n");
        builder.append(nullSafe(execution.request())).append("\n\n");

        builder.append("## Agent Runs\n\n");
        if (execution.agentRuns() == null || execution.agentRuns().isEmpty()) {
            builder.append("No agent runs were recorded.\n\n");
        } else {
            int step = 1;
            for (AgentRunResponse run : execution.agentRuns()) {
                builder.append("### Step ").append(step++).append(": ").append(nullSafe(run.agentName())).append("\n\n");
                builder.append("- Type: ").append(run.agentType()).append("\n");
                builder.append("- Status: ").append(run.status()).append("\n");
                builder.append("- Started: ").append(run.startedAt()).append("\n");
                builder.append("- Completed: ").append(run.completedAt()).append("\n");
                builder.append("- Duration (ms): ").append(run.durationMs()).append("\n");
                builder.append("- Output words: ").append(run.outputWordCount()).append("\n");
                builder.append("- Provider: ").append(nullSafe(run.providerName())).append("\n");
                builder.append("- Model: ").append(nullSafe(run.modelName())).append("\n\n");
                builder.append("#### Input\n\n");
                builder.append(nullSafe(run.input())).append("\n\n");
                builder.append("#### Output\n\n");
                builder.append(nullSafe(run.output())).append("\n\n");
            }
        }

        builder.append("## Final Output\n\n");
        builder.append(nullSafe(execution.finalOutput())).append("\n");
        return builder.toString();
    }

    private String nullSafe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
