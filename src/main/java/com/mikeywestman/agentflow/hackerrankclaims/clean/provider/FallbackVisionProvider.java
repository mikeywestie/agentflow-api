package com.mikeywestman.agentflow.hackerrankclaims.clean.provider;

import com.mikeywestman.agentflow.hackerrankclaims.clean.model.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.Texts;

import java.nio.file.*;
import java.util.*;

public class FallbackVisionProvider implements VisionProvider {
    @Override
    public VisionDecision analyze(ClaimRow row, ClaimPlan plan, UserHistory history, Path imageRoot) {
        boolean valid = !row.imagePaths().isEmpty() && row.imagePaths().stream().allMatch(p -> Files.exists(imageRoot.resolve(p).normalize()));
        List<String> risks = new ArrayList<>(plan.riskFlags());
        risks.add("manual_review_required");
        if (history != null && history.risky()) risks.add("user_history_risk");
        return new VisionDecision(
                false,
                valid ? "Images are present but no multimodal provider was configured." : "Submitted images are missing or invalid.",
                risks,
                Texts.allowedIssue(plan.issueType()),
                Texts.allowedPart(plan.objectPart(), plan.claimObject()),
                "not_enough_information",
                "The image evidence could not be evaluated reliably, so the claim requires manual review.",
                List.of(),
                valid,
                Texts.severityFor(plan.issueType()),
                name(),
                ""
        );
    }

    @Override
    public String name() {
        return "fallback";
    }
}
