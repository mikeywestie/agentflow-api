package com.mikeywestman.agentflow.execution;

import com.mikeywestman.agentflow.agent.AgentEntity;
import com.mikeywestman.agentflow.workflow.WorkflowEntity;
import com.mikeywestman.agentflow.workflow.WorkflowRepository;
import com.mikeywestman.agentflow.workflow.WorkflowStepEntity;
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

        List<WorkflowStepEntity> steps = workflow.getSteps();
        String currentInput = request.request();
        boolean failed = false;
        String failureMessage = null;

        for (int i = 0; i < steps.size(); i++) {
            AgentEntity agent = steps.get(i).getAgent();
            LocalDateTime startedAt = LocalDateTime.now();

            try {
                String output = generateWithRetry(agent, currentInput);
                LocalDateTime completedAt = LocalDateTime.now();

                AgentRunEntity run = buildAgentRun(
                        execution,
                        agent,
                        currentInput,
                        output,
                        ExecutionStatus.COMPLETED,
                        startedAt,
                        completedAt
                );

                execution.getAgentRuns().add(run);
                currentInput = output;
            } catch (RuntimeException ex) {
                failed = true;
                failureMessage = cleanFailureMessage(ex.getMessage());
                LocalDateTime completedAt = LocalDateTime.now();

                AgentRunEntity failedRun = buildAgentRun(
                        execution,
                        agent,
                        currentInput,
                        "Agent failed after retries: " + failureMessage,
                        ExecutionStatus.FAILED,
                        startedAt,
                        completedAt
                );

                execution.getAgentRuns().add(failedRun);
                addSkippedRuns(execution, steps, i + 1, failureMessage);
                break;
            }
        }

        execution.setCompletedAt(LocalDateTime.now());

        if (!failed) {
            execution.setFinalOutput(currentInput);
            execution.setStatus(ExecutionStatus.COMPLETED);
        } else if (hasCompletedAgentRun(execution)) {
            execution.setFinalOutput(
                    "Execution partially completed.\n\n" +
                            "Last successful output:\n" +
                            currentInput +
                            "\n\nFailure reason:\n" +
                            failureMessage
            );
            execution.setStatus(ExecutionStatus.PARTIAL_SUCCESS);
        } else {
            execution.setFinalOutput("Execution failed before any agent completed: " + failureMessage);
            execution.setStatus(ExecutionStatus.FAILED);
        }

        return toResponse(executionRepository.save(execution));
    }

    private AgentRunEntity buildAgentRun(
            ExecutionEntity execution,
            AgentEntity agent,
            String input,
            String output,
            ExecutionStatus status,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        return AgentRunEntity.builder()
                .execution(execution)
                .agent(agent)
                .agentName(agent.getName())
                .agentType(agent.getType())
                .input(input == null ? "" : input)
                .output(output)
                .status(status)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
    }

    private void addSkippedRuns(
            ExecutionEntity execution,
            List<WorkflowStepEntity> steps,
            int startIndex,
            String failureMessage
    ) {
        for (int i = startIndex; i < steps.size(); i++) {
            AgentEntity skippedAgent = steps.get(i).getAgent();
            LocalDateTime timestamp = LocalDateTime.now();

            AgentRunEntity skippedRun = buildAgentRun(
                    execution,
                    skippedAgent,
                    "",
                    "Skipped because a previous agent failed: " + failureMessage,
                    ExecutionStatus.SKIPPED,
                    timestamp,
                    timestamp
            );

            execution.getAgentRuns().add(skippedRun);
        }
    }

    private boolean hasCompletedAgentRun(ExecutionEntity execution) {
        return execution.getAgentRuns()
                .stream()
                .anyMatch(run -> run.getStatus() == ExecutionStatus.COMPLETED);
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
                    sleepBeforeRetry(ex);
                }
            }
        }

        throw lastException;
    }

    private void sleepBeforeRetry(RuntimeException ex) {
        try {
            Thread.sleep(resolveRetryDelayMillis(ex));
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("AI retry interrupted", interruptedException);
        }
    }

    private long resolveRetryDelayMillis(RuntimeException ex) {
        String message = ex.getMessage();

        if (message == null) {
            return 5000;
        }

        if (message.contains("429") || message.contains("Too Many Requests") || message.contains("RESOURCE_EXHAUSTED")) {
            return 30000;
        }

        if (message.contains("503") || message.contains("Service Unavailable") || message.contains("UNAVAILABLE")) {
            return 10000;
        }

        return 5000;
    }

    private String cleanFailureMessage(String message) {
        if (message == null || message.isBlank()) {
            return "AI provider call failed.";
        }

        return message.length() > 1000
                ? message.substring(0, 1000) + "..."
                : message;
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
