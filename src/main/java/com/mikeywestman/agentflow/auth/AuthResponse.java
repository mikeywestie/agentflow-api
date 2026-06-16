package com.mikeywestman.agentflow.auth;

import com.mikeywestman.agentflow.user.UserRole;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String fullName,
        String email,
        UserRole role,
        String token
) {}
