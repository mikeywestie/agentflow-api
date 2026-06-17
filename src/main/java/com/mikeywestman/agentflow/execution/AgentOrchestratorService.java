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
        AiProvider provider = selectProvider();

        ExecutionEntity execution = ExecutionEntity.builder()
                .workflow(workflow)
                .request(request.request())
                .providerName(provider.providerName())
                .modelName(provider.modelName())
                .status(ExecutionStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();

        String currentInput = request.request();
        boolean failed = false;

        for (WorkflowStepEntity step : workflow.getSteps()) {
            AgentEntity agent = step.getAgent();
            LocalDateTime startedAt = LocalDateTime.now();

            try {
                String output = provider.generate(agent.getSystemPrompt(), currentInput);
                LocalDateTime completedAt = LocalDateTime.now();
                execution.getAgentRuns().add(buildRun(execution, agent, currentInput, output, provider.providerName(), provider.modelName(), ExecutionStatus.COMPLETED, startedAt, completedAt));
                currentInput = output;
            } catch (RuntimeException ex) {
                LocalDateTime completedAt = LocalDateTime.now();
                execution.getAgentRuns().add(buildRun(execution, agent, currentInput, ex.getMessage(), provider.providerName(), provider.modelName(), ExecutionStatus.FAILED, startedAt, completedAt));
                failed = true;
                break;
            }
        }

        execution.setCompletedAt(LocalDateTime.now());
        execution.setFinalOutput(currentInput);
        execution.setStatus(failed ? ExecutionStatus.PARTIAL_SUCCESS : ExecutionStatus.COMPLETED);
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

    private AiProvider selectProvider() {
        List<AiProvider> orderedProviders = new ArrayList<>(aiProviders);
        orderedProviders.sort(AnnotationAwareOrderComparator.INSTANCE);

        for (AiProvider provider : orderedProviders) {
            if (provider instanceof StubAiProvider) {
                return provider;
            }
        }

        if (orderedProviders.isEmpty()) {
            throw new IllegalStateException("No AI providers are configured");
        }

        return orderedProviders.get(0);
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
}
