package com.mikeywestman.agentflow.tool;

public record EmailDraftProposal(
        String sourceMessageId,
        String to,
        String subject,
        String body,
        ToolAccessLevel accessLevel,
        String approvalReason
) {}
