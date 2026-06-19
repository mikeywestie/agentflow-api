package com.mikeywestman.agentflow.hackerrankclaims.clean;

import com.mikeywestman.agentflow.hackerrankclaims.clean.model.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.provider.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.service.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.*;

import java.nio.file.Path;
import java.util.*;

public class CleanClaimsCli {
    public static void main(String[] args) throws Exception {
        Config config = Config.from(args);
        List<List<String>> csvRows = CsvFiles.read(config.input());
        if (csvRows.isEmpty()) throw new IllegalArgumentException("Input CSV is empty: " + config.input());
        List<String> headers = csvRows.get(0);
        List<ClaimRow> rows = csvRows.stream().skip(1).map(r -> ClaimRow.from(headers, r)).toList();

        AuditLog audit = new AuditLog(config.audit());
        PlannerService planner = new PlannerService();
        HistoryService historyService = new HistoryService();
        OutputMapper outputMapper = new OutputMapper();
        VisionProvider visionProvider = createProvider(config);
        Map<String, UserHistory> histories = historyService.load(config.imageRoot().resolve("user_history.csv"));
        List<OutputRecord> outputs = new ArrayList<>();

        audit.write("run_started", Map.of(
                "input", config.input().toString(),
                "output", config.output().toString(),
                "imageRoot", config.imageRoot().toString(),
                "visionProvider", visionProvider.name(),
                "rows", rows.size()
        ));

        for (ClaimRow row : rows) {
            ClaimPlan plan = planner.plan(row);
            UserHistory history = histories.get(row.userId());
            VisionDecision decision = visionProvider.analyze(row, plan, history, config.imageRoot());
            OutputRecord output = outputMapper.map(row, plan, decision);
            outputs.add(output);
            audit.write("claim_reviewed", Map.of(
                    "user_id", row.userId(),
                    "plan", plan.audit(),
                    "decision", decision.audit(),
                    "output", output.values()
            ));
        }

        CsvFiles.write(config.output(), OutputMapper.OUTPUT_COLUMNS,
                outputs.stream().map(o -> OutputMapper.OUTPUT_COLUMNS.stream().map(c -> o.values().getOrDefault(c, "")).toList()).toList());
        audit.write("run_finished", Map.of("output", config.output().toString(), "rows", outputs.size()));

        if (config.expected() != null) {
            new Evaluator().evaluate(config.expected(), outputs, audit);
        }

        System.out.println("Wrote " + outputs.size() + " rows to " + config.output().toAbsolutePath());
        System.out.println("Audit log: " + config.audit().toAbsolutePath());
    }

    private static VisionProvider createProvider(Config config) {
        if ("github".equalsIgnoreCase(config.vision())) {
            return new GitHubModelsVisionProvider(System.getenv("GITHUB_TOKEN"), config.githubModel());
        }
        return new FallbackVisionProvider();
    }

    record Config(Path input, Path output, Path imageRoot, Path audit, Path expected, String vision, String githubModel) {
        static Config from(String[] args) {
            Map<String, String> values = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("--")) {
                    String key = args[i].substring(2);
                    String value = (i + 1 < args.length && !args[i + 1].startsWith("--")) ? args[++i] : "true";
                    values.put(key, value);
                }
            }
            String defaultVision = System.getenv("GITHUB_TOKEN") == null ? "fallback" : "github";
            return new Config(
                    Path.of(values.getOrDefault("input", "dataset/claims.csv")),
                    Path.of(values.getOrDefault("output", "output.csv")),
                    Path.of(values.getOrDefault("image-root", "dataset")),
                    Path.of(values.getOrDefault("audit", "log.txt")),
                    values.containsKey("expected") ? Path.of(values.get("expected")) : null,
                    values.getOrDefault("vision", defaultVision),
                    values.getOrDefault("github-model", "openai/gpt-4o-mini")
            );
        }
    }
}
