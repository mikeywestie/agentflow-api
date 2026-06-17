package com.mikeywestman.agentflow.tool;

import java.time.LocalDateTime;

public record EmailToolMessage(
        String id,
        String fromName,
        String fromEmail,
        String subject,
        String snippet,
        String body,
        boolean unread,
        boolean hasAttachment,
        LocalDateTime receivedAt
) {}
