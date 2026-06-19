package com.mikeywestman.agentflow.hackerrankclaims.clean.service;

import com.mikeywestman.agentflow.hackerrankclaims.clean.model.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.CsvFiles;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.Texts;

import java.nio.file.*;
import java.util.*;

public class HistoryService {
    public Map<String, UserHistory> load(Path path) {
        try {
            if (!Files.exists(path)) return Map.of();
            List<List<String>> rows = CsvFiles.read(path);
            if (rows.isEmpty()) return Map.of();
            List<String> headers = rows.get(0);
            Map<String, UserHistory> histories = new HashMap<>();
            for (List<String> values : rows.stream().skip(1).toList()) {
                ClaimRow row = ClaimRow.from(headers, values);
                histories.put(row.get("user_id"), new UserHistory(
                        number(row.get("past_claim_count")),
                        number(row.get("last_90_days_claim_count")),
                        row.get("history_flags"),
                        row.get("history_summary")
                ));
            }
            return histories;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private int number(String value) {
        try {
            return Texts.notBlank(value) ? Integer.parseInt(value.trim()) : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
