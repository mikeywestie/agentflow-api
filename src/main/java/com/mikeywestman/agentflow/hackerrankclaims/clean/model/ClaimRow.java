package com.mikeywestman.agentflow.hackerrankclaims.clean.model;

import com.mikeywestman.agentflow.hackerrankclaims.clean.util.Texts;

import java.util.*;

public record ClaimRow(Map<String, String> values) {
    public static ClaimRow from(List<String> headers, List<String> row) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            values.put(headers.get(i), i < row.size() ? row.get(i) : "");
        }
        return new ClaimRow(values);
    }

    public String get(String... names) {
        for (String name : names) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (Texts.norm(entry.getKey()).equals(Texts.norm(name)) && Texts.notBlank(entry.getValue())) {
                    return entry.getValue().trim();
                }
            }
        }
        return "";
    }

    public String userId() {
        return get("user_id", "id", "claim_id", "case_id");
    }

    public String imagePathsRaw() {
        return get("image_paths", "images", "image_path", "submitted_images");
    }

    public List<String> imagePaths() {
        return Texts.splitMulti(imagePathsRaw());
    }

    public String userClaim() {
        String claim = get("user_claim", "claim", "conversation", "claim_conversation", "messages", "chat", "transcript");
        return Texts.notBlank(claim) ? claim : String.join(" ", values.values());
    }

    public String claimObject() {
        return Texts.allowedObject(get("claim_object", "object", "object_type", "item_type", "category"));
    }
}
