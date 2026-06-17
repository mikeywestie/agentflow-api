package com.mikeywestman.agentflow.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tools/email/workflows")
@RequiredArgsConstructor
public class EmailWorkflowController {

    private final EmailWorkflowService emailWorkflowService;

    @PostMapping("/run")
    public EmailWorkflowResponse run(@RequestBody EmailWorkflowRequest request) {
        return emailWorkflowService.run(request);
    }
}
