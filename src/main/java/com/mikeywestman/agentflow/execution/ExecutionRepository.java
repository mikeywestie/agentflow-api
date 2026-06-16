package com.mikeywestman.agentflow.execution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, UUID> {
    List<ExecutionEntity> findTop20ByOrderByStartedAtDesc();

    @Query("""
        SELECT DISTINCT e
        FROM ExecutionEntity e
        LEFT JOIN FETCH e.workflow
        LEFT JOIN FETCH e.agentRuns
        ORDER BY e.startedAt DESC
    """)
    List<ExecutionEntity> findLatestWithWorkflow();
}
