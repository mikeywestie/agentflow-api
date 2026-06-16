package com.mikeywestman.agentflow.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<AgentEntity, UUID> {
    List<AgentEntity> findByEnabledTrueOrderByNameAsc();
}
