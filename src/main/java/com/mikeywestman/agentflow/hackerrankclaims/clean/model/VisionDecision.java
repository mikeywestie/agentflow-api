package com.mikeywestman.agentflow.hackerrankclaims.clean.model;

import java.util.List;
import java.util.Map;

public record VisionDecision(
        boolean evidenceStandardMet,
        String evidenceStandardMetReason,
        List<String> riskFlags,
        String issueType,
        String objectPart,
        String claimStatus,
        String claimStatusJustification,
        List<String> supportingImageIds,
        boolean validImage,
        String severity,
        String provider,
        String rawModelOutput
) {
    public Map<String, Object> audit() {
        return Map.of(
                "evidenceStandardMet", evidenceStandardMet,
                "riskFlags", riskFlags,
                "issueType", issueType,
                "objectPart", objectPart,
                "claimStatus", claimStatus,
                "supportingImageIds", supportingImageIds,
                "validImage", validImage,
                "severity", severity,
                "provider", provider
        );
    }
}
