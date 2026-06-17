package com.mikeywestman.agentflow.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools/email")
@RequiredArgsConstructor
public class EmailToolController {

    private final EmailToolService emailToolService;

    @GetMapping("/messages")
    public List<EmailToolMessage> searchMessages(@RequestParam(defaultValue = "") String query) {
        return emailToolService.search(query);
    }

    @GetMapping("/messages/{id}")
    public EmailToolMessage readMessage(@PathVariable String id) {
        return emailToolService.read(id);
    }

    @PostMapping("/messages/{id}/proposal")
    public EmailDraftProposal createProposal(@PathVariable String id) {
        return emailToolService.proposeReply(id);
    }
}
