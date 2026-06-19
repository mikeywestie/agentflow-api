package com.mikeywestman.agentflow.hackerrankclaims.clean.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public final class CsvFiles {
    private CsvFiles() {}

    public static List<List<String>> read(Path path) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) rows.add(parse(line));
        }
        return rows;
    }

    public static void write(Path path, List<String> headers, List<List<String>> rows) throws IOException {
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(headers.stream().map(CsvFiles::escape).collect(Collectors.joining(",")));
            writer.newLine();
            for (List<String> row : rows) {
                writer.write(row.stream().map(CsvFiles::escape).collect(Collectors.joining(",")));
                writer.newLine();
            }
        }
    }

    private static List<String> parse(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                result.add(current.toString());
                current.setLength(0);
            } else current.append(ch);
        }
        result.add(current.toString());
        return result;
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\n") || safe.contains("\r") || safe.contains("\"")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}
