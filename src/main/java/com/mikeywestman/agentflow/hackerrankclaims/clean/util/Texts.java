package com.mikeywestman.agentflow.hackerrankclaims.clean.util;

import java.text.Normalizer;
import java.util.*;

public final class Texts {
    private Texts() {}

    public static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String norm(String value) {
        if (value == null) return "";
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return ascii.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    public static List<String> splitMulti(String raw) {
        if (!notBlank(raw)) return List.of();
        return Arrays.stream(raw.split("\\s*[;|]\\s*|\\s*,\\s*"))
                .map(String::trim)
                .filter(Texts::notBlank)
                .toList();
    }

    public static boolean containsAny(String text, String... needles) {
        String safe = text == null ? "" : text;
        for (String needle : needles) {
            if (safe.contains(needle)) return true;
        }
        return false;
    }

    public static String clean(String value) {
        String cleaned = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 280 ? cleaned.substring(0, 277) + "..." : cleaned;
    }

    public static String allowedObject(String value) {
        String n = norm(value);
        return Set.of("car", "laptop", "package").contains(n) ? n : "unknown";
    }

    public static String allowedIssue(String value) {
        String n = norm(value);
        if (Set.of("shattered", "shatter", "broken_glass", "glass").contains(n)) return "glass_shatter";
        if (Set.of("tear", "torn", "ripped").contains(n)) return "torn_packaging";
        if (Set.of("crushed", "crush").contains(n)) return "crushed_packaging";
        if (Set.of("missing", "missing_contents").contains(n)) return "missing_part";
        if (Set.of("broken", "damage", "damaged", "damaged_part").contains(n)) return "broken_part";
        return Set.of("dent", "scratch", "crack", "glass_shatter", "broken_part", "missing_part", "torn_packaging", "crushed_packaging", "water_damage", "stain", "none", "unknown").contains(n) ? n : "unknown";
    }

    public static String allowedPart(String value, String object) {
        String n = norm(value);
        if (n.equals("mirror")) return "side_mirror";
        if ("package".equals(object) && n.equals("corner")) return "package_corner";
        if ("package".equals(object) && n.equals("side")) return "package_side";
        Set<String> car = Set.of("front_bumper", "rear_bumper", "door", "hood", "windshield", "side_mirror", "headlight", "taillight", "fender", "quarter_panel", "body", "unknown");
        Set<String> laptop = Set.of("screen", "keyboard", "trackpad", "hinge", "lid", "corner", "port", "base", "body", "unknown");
        Set<String> pack = Set.of("box", "package_corner", "package_side", "seal", "label", "contents", "item", "unknown");
        Set<String> allowed = "car".equals(object) ? car : "laptop".equals(object) ? laptop : pack;
        return allowed.contains(n) ? n : "unknown";
    }

    public static String allowedStatus(String value) {
        String n = norm(value);
        return Set.of("supported", "contradicted", "not_enough_information").contains(n) ? n : "not_enough_information";
    }

    public static String allowedSeverity(String value) {
        String n = norm(value);
        return Set.of("none", "low", "medium", "high", "unknown").contains(n) ? n : "unknown";
    }

    public static String severityFor(String issue) {
        return switch (allowedIssue(issue)) {
            case "none" -> "none";
            case "scratch", "stain" -> "low";
            case "dent", "crack", "broken_part", "torn_packaging", "water_damage" -> "medium";
            case "glass_shatter", "missing_part", "crushed_packaging" -> "high";
            default -> "unknown";
        };
    }

    public static String allowedRisk(String value) {
        String n = norm(value);
        if (Set.of("blurry", "blurred").contains(n)) return "blurry_image";
        if (Set.of("cropped", "obstructed").contains(n)) return "cropped_or_obstructed";
        if (Set.of("low_light", "glare").contains(n)) return "low_light_or_glare";
        return Set.of("none", "blurry_image", "cropped_or_obstructed", "low_light_or_glare", "wrong_angle", "wrong_object", "wrong_object_part", "damage_not_visible", "claim_mismatch", "possible_manipulation", "non_original_image", "text_instruction_present", "user_history_risk", "manual_review_required").contains(n) ? n : "manual_review_required";
    }

    public static String normalizeRisks(Collection<String> risks) {
        List<String> normalized = risks.stream()
                .map(Texts::allowedRisk)
                .filter(r -> !"none".equals(r))
                .distinct()
                .toList();
        return normalized.isEmpty() ? "none" : String.join(";", normalized);
    }
}
