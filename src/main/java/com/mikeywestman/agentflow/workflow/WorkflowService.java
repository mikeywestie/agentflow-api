package com.mikeywestman.agentflow.workflow;

import com.mikeywestman.agentflow.agent.AgentEntity;
import com.mikeywestman.agentflow.agent.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final AgentRepository agentRepository;

    public List<WorkflowResponse> findAll() {
        return workflowRepository.findAll().stream().map(this::toResponse).toList();
    }

    public WorkflowResponse findById(UUID id) {
        return workflowRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + id));
    }

    @Transactional
    public WorkflowResponse create(WorkflowRequest request) {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .name(request.name())
                .description(request.description())
                .enabled(request.enabled())
                .steps(new ArrayList<>())
                .build();

        applySteps(workflow, request.agentIds());
        return toResponse(workflowRepository.save(workflow));
    }

    @Transactional
    public WorkflowResponse update(UUID id, WorkflowRequest request) {
        WorkflowEntity workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + id));

        workflow.setName(request.name());
        workflow.setDescription(request.description());
        workflow.setEnabled(request.enabled());
        workflow.getSteps().clear();
        applySteps(workflow, request.agentIds());

        return toResponse(workflowRepository.save(workflow));
    }

    @Transactional
    public WorkflowResponse setEnabled(UUID id, boolean enabled) {
        WorkflowEntity workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + id));

        workflow.setEnabled(enabled);
        return toResponse(workflowRepository.save(workflow));
    }

    @Transactional
    public void delete(UUID id) {
        if (!workflowRepository.existsById(id)) {
            throw new IllegalArgumentException("Workflow not found: " + id);
        }

        workflowRepository.deleteById(id);
    }

    private void applySteps(WorkflowEntity workflow, List<UUID> agentIds) {
        int order = 1;

        for (UUID agentId : agentIds) {
            AgentEntity agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

            workflow.getSteps().add(WorkflowStepEntity.builder()
                    .workflow(workflow)
                    .agent(agent)
                    .stepOrder(order++)
                    .build());
        }
    }

    public WorkflowResponse toResponse(WorkflowEntity workflow) {
        return new WorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.isEnabled(),
                workflow.getSteps().stream()
                        .map(step -> new WorkflowStepResponse(
                                step.getId(),
                                step.getStepOrder(),
                                step.getAgent().getId(),
                                step.getAgent().getName(),
                                step.getAgent().getType()
                        ))
                        .toList()
        );
    }
}
