package com.mikeywestman.agentflow.hackerrankclaims.clean.provider;

import com.mikeywestman.agentflow.hackerrankclaims.clean.model.ClaimPlan;
import com.mikeywestman.agentflow.hackerrankclaims.clean.model.ClaimRow;
import com.mikeywestman.agentflow.hackerrankclaims.clean.model.UserHistory;
import com.mikeywestman.agentflow.hackerrankclaims.clean.model.VisionDecision;

import java.nio.file.Path;

public interface VisionProvider {
    VisionDecision analyze(ClaimRow row, ClaimPlan plan, UserHistory history, Path imageRoot) throws Exception;
    String name();
}
