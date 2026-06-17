package com.mikeywestman.agentflow.execution;

import com.mikeywestman.agentflow.agent.AgentEntity;
import com.mikeywestman.agentflow.workflow.WorkflowEntity;
import com.mikeywestman.agentflow.workflow.WorkflowRepository;
import com.mikeywestman.agentflow.workflow.WorkflowStepEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentOrchestratorService {

    private final WorkflowRepository workflowRepository;
    private final ExecutionRepository executionRepository;
    private final List<AiProvider> aiProviders;

    @Transactional
    public ExecutionResponse run(RunExecutionRequest request) {
        WorkflowEntity workflow = resolveWorkflow(request.workflowId());

        ExecutionEntity execution = ExecutionEntity.builder()
                .workflow(workflow)
                .request(request.request())
                .status(ExecutionStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();

        String currentInput = request.request();
        boolean failed = false;

        for (WorkflowStepEntity step : workflow.getSteps()) {
            AgentEntity agent = step.getAgent();
            LocalDateTime startedAt = LocalDateTime.now();

            try {
                ProviderResult result = generateWithFallback(agent, currentInput);
                LocalDateTime completedAt = LocalDateTime.now();
                execution.getAgentRuns().add(buildRun(execution, agent, currentInput, result.output(), result.providerName(), result.modelName(), ExecutionStatus.COMPLETED, startedAt, completedAt));
                currentInput = result.output();
            } catch (RuntimeException ex) {
                LocalDateTime completedAt = LocalDateTime.now();
                execution.getAgentRuns().add(buildRun(execution, agent, currentInput, ex.getMessage(), null, null, ExecutionStatus.FAILED, startedAt, completedAt));
                failed = true;
                break;
            }
        }

        execution.setCompletedAt(LocalDateTime.now());
        execution.setFinalOutput(currentInput);
        execution.setStatus(failed ? ExecutionStatus.PARTIAL_SUCCESS : ExecutionStatus.COMPLETED);
        applyExecutionProviderSummary(execution);
        return toResponse(executionRepository.save(execution));
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> findLatest() {
        return executionRepository.findLatestWithWorkflow().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExecutionResponse findById(UUID id) {
        return executionRepository.findById(id).map(this::toResponse).orElseThrow(() -> new IllegalArgumentException("Execution not found"));
    }

    private WorkflowEntity resolveWorkflow(UUID workflowId) {
        if (workflowId != null) {
            return workflowRepository.findById(workflowId).orElseThrow(() -> new IllegalArgumentException("Workflow not found"));
        }
        return workflowRepository.findByEnabledTrueOrderByNameAsc().stream().findFirst().orElseThrow(() -> new IllegalArgumentException("No enabled workflow found"));
    }

    private ProviderResult generateWithFallback(AgentEntity agent, String input) {
        List<AiProvider> orderedProviders = new ArrayList<>(aiProviders);
        orderedProviders.sort(AnnotationAwareOrderComparator.INSTANCE);

        List<String> errors = new ArrayList<>();
        for (AiProvider provider : orderedProviders) {
            int attempts = provider instanceof GeminiAiProvider ? 3 : 1;
            for (int attempt = 1; attempt <= attempts; attempt++) {
                try {
                    String output = provider.generate(agent.getSystemPrompt(), input);
                    return new ProviderResult(output, provider.providerName(), provider.modelName());
                } catch (RuntimeException ex) {
                    errors.add(provider.providerName() + " attempt " + attempt + ": " + ex.getMessage());
                    if (attempt < attempts) {
                        sleepBeforeRetry();
                    }
                }
            }
        }

        throw new RuntimeException("All AI providers failed: " + String.join(" | ", errors));
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("AI retry interrupted", ex);
        }
    }

    private void applyExecutionProviderSummary(ExecutionEntity execution) {
        List<String> providers = execution.getAgentRuns().stream()
                .map(AgentRunEntity::getProviderName)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
        List<String> models = execution.getAgentRuns().stream()
                .map(AgentRunEntity::getModelName)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();

        execution.setProviderName(providers.isEmpty() ? null : providers.size() == 1 ? providers.get(0) : "Mixed");
        execution.setModelName(models.isEmpty() ? null : models.size() == 1 ? models.get(0) : "Mixed");
    }

    private AgentRunEntity buildRun(ExecutionEntity execution, AgentEntity agent, String input, String output, String providerName, String modelName, ExecutionStatus status, LocalDateTime startedAt, LocalDateTime completedAt) {
        return AgentRunEntity.builder()
                .execution(execution)
                .agent(agent)
                .agentName(agent.getName())
                .agentType(agent.getType())
                .input(input == null ? "" : input)
                .output(output == null ? "" : output)
                .providerName(providerName)
                .modelName(modelName)
                .status(status)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
    }

    private ExecutionResponse toResponse(ExecutionEntity execution) {
        return new ExecutionResponse(
                execution.getId(),
                execution.getWorkflow() == null ? null : execution.getWorkflow().getId(),
                execution.getWorkflow() == null ? null : execution.getWorkflow().getName(),
                execution.getRequest(),
                execution.getFinalOutput(),
                execution.getStatus(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getAgentRuns().stream().map(run -> new AgentRunResponse(
                        run.getId(),
                        run.getAgentName(),
                        run.getAgentType(),
                        run.getInput(),
                        run.getOutput(),
                        run.getStatus(),
                        run.getStartedAt(),
                        run.getCompletedAt(),
                        durationMs(run.getStartedAt(), run.getCompletedAt()),
                        countWords(run.getOutput()),
                        run.getProviderName(),
                        run.getModelName()
                )).toList()
        );
    }

    private Long durationMs(LocalDateTime startedAt, LocalDateTime completedAt) {
        if (startedAt == null || completedAt == null) return null;
        return Duration.between(startedAt, completedAt).toMillis();
    }

    private int countWords(String value) {
        if (value == null || value.isBlank()) return 0;
        return value.trim().split("\\s+").length;
    }

    private record ProviderResult(String output, String providerName, String modelName) {}
}
