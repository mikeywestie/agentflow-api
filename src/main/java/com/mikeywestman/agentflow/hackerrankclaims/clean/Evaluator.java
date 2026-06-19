package com.mikeywestman.agentflow.hackerrankclaims.clean;

import com.mikeywestman.agentflow.hackerrankclaims.clean.model.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.service.OutputMapper;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.CsvFiles;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.Texts;

import java.nio.file.Path;
import java.util.*;

public class Evaluator {
    public void evaluate(Path expectedPath, List<OutputRecord> outputs, AuditLog audit) throws Exception {
        List<List<String>> expectedRows = CsvFiles.read(expectedPath);
        if (expectedRows.isEmpty()) return;
        List<String> headers = expectedRows.get(0);
        Map<String, ClaimRow> expected = new HashMap<>();
        for (List<String> rowValues : expectedRows.stream().skip(1).toList()) {
            ClaimRow row = ClaimRow.from(headers, rowValues);
            expected.put(row.userId(), row);
        }
        for (String column : List.of("evidence_standard_met", "issue_type", "object_part", "claim_status", "valid_image", "severity")) {
            int total = 0;
            int correct = 0;
            for (OutputRecord output : outputs) {
                ClaimRow expectedRow = expected.get(output.values().get("user_id"));
                if (expectedRow == null || !Texts.notBlank(expectedRow.get(column))) continue;
                total++;
                if (Texts.norm(expectedRow.get(column)).equals(Texts.norm(output.values().get(column)))) correct++;
            }
            String result = total == 0 ? "n/a" : String.format(Locale.ROOT, "%.2f%%", correct * 100.0 / total);
            System.out.println(" - " + column + ": " + correct + "/" + total + " = " + result);
            audit.write("eval_" + column, Map.of("correct", correct, "total", total));
        }
    }
}
