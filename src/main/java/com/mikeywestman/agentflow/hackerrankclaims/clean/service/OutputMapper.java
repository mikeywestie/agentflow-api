package com.mikeywestman.agentflow.hackerrankclaims.clean.service;

import com.mikeywestman.agentflow.hackerrankclaims.clean.model.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.Texts;

import java.util.*;

public class OutputMapper {
    public static final List<String> OUTPUT_COLUMNS = List.of(
            "user_id",
            "image_paths",
            "user_claim",
            "claim_object",
            "evidence_standard_met",
            "evidence_standard_met_reason",
            "risk_flags",
            "issue_type",
            "object_part",
            "claim_status",
            "claim_status_justification",
            "supporting_image_ids",
            "valid_image",
            "severity"
    );

    public OutputRecord map(ClaimRow row, ClaimPlan plan, VisionDecision decision) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("user_id", row.userId());
        values.put("image_paths", String.join(";", row.imagePaths()));
        values.put("user_claim", row.userClaim());
        values.put("claim_object", Texts.allowedObject(plan.claimObject()));
        values.put("evidence_standard_met", String.valueOf(decision.evidenceStandardMet()));
        values.put("evidence_standard_met_reason", Texts.clean(decision.evidenceStandardMetReason()));
        values.put("risk_flags", Texts.normalizeRisks(decision.riskFlags()));
        values.put("issue_type", Texts.allowedIssue(decision.issueType()));
        values.put("object_part", Texts.allowedPart(decision.objectPart(), plan.claimObject()));
        values.put("claim_status", Texts.allowedStatus(decision.claimStatus()));
        values.put("claim_status_justification", Texts.clean(decision.claimStatusJustification()));
        values.put("supporting_image_ids", decision.supportingImageIds().isEmpty() ? "none" : String.join(";", decision.supportingImageIds()));
        values.put("valid_image", String.valueOf(decision.validImage()));
        values.put("severity", Texts.allowedSeverity(decision.severity()));
        return new OutputRecord(values);
    }
}
