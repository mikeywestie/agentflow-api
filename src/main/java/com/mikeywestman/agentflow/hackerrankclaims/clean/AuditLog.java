package com.mikeywestman.agentflow.hackerrankclaims.clean;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.Map;

public class AuditLog {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Path path;

    public AuditLog(Path path) {
        this.path = path;
    }

    public synchronized void write(String event, Object payload) {
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Map<String, Object> row = Map.of(
                    "timestamp", Instant.now().toString(),
                    "event", event,
                    "payload", payload
            );
            Files.writeString(path, JSON.writeValueAsString(row) + System.lineSeparator(), StandardCharsets.UTF_8,
                    Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (Exception ignored) {
        }
    }
}
