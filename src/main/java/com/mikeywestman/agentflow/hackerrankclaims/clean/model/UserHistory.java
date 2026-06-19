package com.mikeywestman.agentflow.hackerrankclaims.clean.model;

public record UserHistory(int pastClaimCount, int recentClaimCount, String flags, String summary) {
    public boolean risky() {
        String lower = (flags == null ? "" : flags).toLowerCase();
        return recentClaimCount >= 3 || lower.contains("risk") || lower.contains("duplicate");
    }
}
