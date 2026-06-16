package com.mikeywestman.agentflow.config;

import com.mikeywestman.agentflow.agent.AgentEntity;
import com.mikeywestman.agentflow.agent.AgentRepository;
import com.mikeywestman.agentflow.agent.AgentType;
import com.mikeywestman.agentflow.workflow.WorkflowEntity;
import com.mikeywestman.agentflow.workflow.WorkflowRepository;
import com.mikeywestman.agentflow.workflow.WorkflowStepEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AgentRepository agentRepository;
    private final WorkflowRepository workflowRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!agentRepository.findAll().isEmpty()) {
            return;
        }

        AgentEntity planner = agentRepository.save(AgentEntity.builder()
                .name("Planner Agent")
                .description("Breaks a request into clear tasks and requirements.")
                .type(AgentType.PLANNER)
                .systemPrompt("You are a senior software planner. Break the request into requirements, risks, and execution steps.")
                .enabled(true)
                .build());

        AgentEntity builder = agentRepository.save(AgentEntity.builder()
                .name("Builder Agent")
                .description("Turns the plan into a practical software architecture.")
                .type(AgentType.BUILDER)
                .systemPrompt("You are a full-stack Java architect. Convert the plan into entities, endpoints, services, screens, and implementation notes.")
                .enabled(true)
                .build());

        AgentEntity reviewer = agentRepository.save(AgentEntity.builder()
                .name("Reviewer Agent")
                .description("Reviews the generated solution and improves it.")
                .type(AgentType.REVIEWER)
                .systemPrompt("You are a strict technical reviewer. Find gaps, security issues, missing edge cases, and improvements. End with a polished final answer.")
                .enabled(true)
                .build());

        WorkflowEntity workflow = WorkflowEntity.builder()
                .name("Software Architecture Workflow")
                .description("Planner → Builder → Reviewer pipeline for software project requests.")
                .enabled(true)
                .steps(new ArrayList<>())
                .build();

        List<AgentEntity> agents = List.of(planner, builder, reviewer);
        for (int i = 0; i < agents.size(); i++) {
            workflow.getSteps().add(WorkflowStepEntity.builder()
                    .workflow(workflow)
                    .agent(agents.get(i))
                    .stepOrder(i + 1)
                    .build());
        }

        workflowRepository.save(workflow);
    }
}
