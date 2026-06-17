package com.mikeywestman.agentflow.tool;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EmailToolService {

    private final List<EmailToolMessage> messages = List.of(
            new EmailToolMessage(
                    "email-001",
                    "Jordan Todd",
                    "jordan@example.com",
                    "Tech Lead Opportunity",
                    "I am reaching out about a Java Tech Lead opportunity with a marketplace platform modernization project.",
                    "Hi Michael, I am reaching out about a Java Tech Lead opportunity with a marketplace platform modernization project. The role involves Java, Spring Boot, AWS, and legacy modernization. Would you be open to a quick call?",
                    true,
                    false,
                    LocalDateTime.now().minusHours(3)
            ),
            new EmailToolMessage(
                    "email-002",
                    "N & S Services",
                    "admin@nands.example.com",
                    "Quotation request for electrical work",
                    "Please prepare a quote for lights, sockets, conduit, and compliance work.",
                    "Hi Michael, please prepare a quote for lights, sockets, conduit, and compliance work. We need a clear breakdown of materials, labour, and total cost.",
                    true,
                    true,
                    LocalDateTime.now().minusHours(5)
            ),
            new EmailToolMessage(
                    "email-003",
                    "Portfolio Contact",
                    "hello@example.com",
                    "Question about your AgentFlow project",
                    "I saw your AgentFlow project and wanted to know whether it supports email workflows.",
                    "Hi Michael, I saw your AgentFlow project and wanted to know whether it supports email workflows and agent tool usage. Could you share more information?",
                    false,
                    false,
                    LocalDateTime.now().minusDays(1)
            )
    );

    public List<EmailToolMessage> search(String query) {
        if (query == null || query.isBlank()) {
            return messages;
        }

        String normalizedQuery = query.toLowerCase(Locale.ROOT);

        return messages.stream()
                .filter(message -> contains(message.subject(), normalizedQuery)
                        || contains(message.snippet(), normalizedQuery)
                        || contains(message.body(), normalizedQuery)
                        || contains(message.fromName(), normalizedQuery)
                        || contains(message.fromEmail(), normalizedQuery))
                .toList();
    }

    public EmailToolMessage read(String id) {
        return messages.stream()
                .filter(message -> message.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Email message not found: " + id));
    }

    public EmailDraftProposal proposeReply(String id) {
        EmailToolMessage message = read(id);
        String body = "Hi " + message.fromName() + ",\n\n"
                + "Thank you for reaching out. I have reviewed your message and I am interested in discussing the next steps.\n\n"
                + "Kind regards,\nMichael";

        return new EmailDraftProposal(
                message.id(),
                message.fromEmail(),
                "Re: " + message.subject(),
                body,
                ToolAccessLevel.APPROVAL_REQUIRED,
                "Creating or sending email replies should require human approval before any external action is taken."
        );
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
