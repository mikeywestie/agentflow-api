package com.mikeywestman.agentflow.hackerrankclaims.clean.model;

import java.util.List;
import java.util.Map;

public record ClaimPlan(String claimObject, String issueType, String objectPart, List<String> riskFlags) {
    public Map<String, Object> audit() {
        return Map.of(
                "claimObject", claimObject,
                "issueType", issueType,
                "objectPart", objectPart,
                "riskFlags", riskFlags
        );
    }
}
