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

    @Transactional
    public WorkflowResponse create(WorkflowRequest request) {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .name(request.name())
                .description(request.description())
                .enabled(request.enabled())
                .steps(new ArrayList<>())
                .build();

        int order = 1;
        for (UUID agentId : request.agentIds()) {
            AgentEntity agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

            workflow.getSteps().add(WorkflowStepEntity.builder()
                    .workflow(workflow)
                    .agent(agent)
                    .stepOrder(order++)
                    .build());
        }

        return toResponse(workflowRepository.save(workflow));
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
