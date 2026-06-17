package com.mikeywestman.agentflow.tool;

import com.mikeywestman.agentflow.execution.AgentOrchestratorService;
import com.mikeywestman.agentflow.execution.ExecutionResponse;
import com.mikeywestman.agentflow.execution.RunExecutionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailWorkflowService {

    private final EmailToolService emailToolService;
    private final AgentOrchestratorService orchestratorService;

    public EmailWorkflowResponse run(EmailWorkflowRequest request) {
        List<EmailToolMessage> messages = emailToolService.search(request.query());
        String workflowInput = buildWorkflowInput(request.query(), messages);
        ExecutionResponse execution = orchestratorService.run(new RunExecutionRequest(request.workflowId(), workflowInput));

        return new EmailWorkflowResponse(
                request.query(),
                messages.size(),
                ToolAccessLevel.READ_ONLY,
                "This workflow only reads local email tool data. Any reply, archive, delete, or send action must be handled as a separate approval-required step.",
                messages,
                execution
        );
    }

    private String buildWorkflowInput(String query, List<EmailToolMessage> messages) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are analyzing email tool results for AgentFlow.\n\n");
        builder.append("User email search query: ").append(query == null || query.isBlank() ? "all messages" : query).append("\n\n");
        builder.append("Important safety rule: Only summarize, classify, and recommend actions. Do not claim that any email was sent, archived, deleted, or modified.\n\n");
        builder.append("Emails found: ").append(messages.size()).append("\n\n");

        for (EmailToolMessage message : messages) {
            builder.append("---\n");
            builder.append("Message ID: ").append(message.id()).append("\n");
            builder.append("From: ").append(message.fromName()).append(" <").append(message.fromEmail()).append(">\n");
            builder.append("Subject: ").append(message.subject()).append("\n");
            builder.append("Unread: ").append(message.unread()).append("\n");
            builder.append("Has attachment: ").append(message.hasAttachment()).append("\n");
            builder.append("Received: ").append(message.receivedAt()).append("\n");
            builder.append("Snippet: ").append(message.snippet()).append("\n");
            builder.append("Body: ").append(message.body()).append("\n\n");
        }

        builder.append("Return a structured email triage report with urgency, category, recommended next action, and whether approval is required.\n");
        return builder.toString();
    }
}
