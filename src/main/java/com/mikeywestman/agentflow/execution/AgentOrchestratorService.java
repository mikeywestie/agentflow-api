package com.mikeywestman.agentflow.execution;

import com.mikeywestman.agentflow.agent.AgentEntity;
import com.mikeywestman.agentflow.workflow.WorkflowEntity;
import com.mikeywestman.agentflow.workflow.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentOrchestratorService {

    private final WorkflowRepository workflowRepository;
    private final ExecutionRepository executionRepository;
    private final AiProvider aiProvider;

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

        try {
            for (var step : workflow.getSteps()) {
                AgentEntity agent = step.getAgent();

                LocalDateTime startedAt = LocalDateTime.now();

                String output = generateWithRetry(agent, currentInput);

                LocalDateTime completedAt = LocalDateTime.now();

                AgentRunEntity run = AgentRunEntity.builder()
                        .execution(execution)
                        .agent(agent)
                        .agentName(agent.getName())
                        .agentType(agent.getType())
                        .input(currentInput)
                        .output(output)
                        .status(ExecutionStatus.COMPLETED)
                        .startedAt(startedAt)
                        .completedAt(completedAt)
                        .build();

                execution.getAgentRuns().add(run);
                currentInput = output;
            }

            execution.setFinalOutput(currentInput);
            execution.setStatus(ExecutionStatus.COMPLETED);
            execution.setCompletedAt(LocalDateTime.now());

        } catch (RuntimeException ex) {
            execution.setFinalOutput("Execution failed: " + ex.getMessage());
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setCompletedAt(LocalDateTime.now());
        }

        return toResponse(executionRepository.save(execution));
    }

    private String generateWithRetry(AgentEntity agent, String input) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return aiProvider.generate(
                        agent.getSystemPrompt(),
                        input
                );
            } catch (RuntimeException ex) {
                lastException = ex;

                System.out.println(
                        "AI attempt " + attempt +
                                " failed for agent " + agent.getName() +
                                ": " + ex.getMessage()
                );

                if (attempt < 3) {
                    sleepBeforeRetry();
                }
            }
        }

        throw lastException;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("AI retry interrupted", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponse> findLatest() {
        return executionRepository.findLatestWithWorkflow()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExecutionResponse findById(UUID id) {
        ExecutionEntity execution = executionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + id));

        return toResponse(execution);
    }

    private WorkflowEntity resolveWorkflow(UUID workflowId) {
        if (workflowId != null) {
            return workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + workflowId));
        }

        return workflowRepository.findByEnabledTrueOrderByNameAsc().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enabled workflow found. Create one first."));
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
                execution.getAgentRuns().stream()
                        .map(run -> new AgentRunResponse(
                                run.getId(),
                                run.getAgentName(),
                                run.getAgentType(),
                                run.getInput(),
                                run.getOutput(),
                                run.getStatus(),
                                run.getStartedAt(),
                                run.getCompletedAt()
                        ))
                        .toList()
        );
    }
}