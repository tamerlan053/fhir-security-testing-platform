package com.fhir.security.attack;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Best-effort, offline-safe heuristics on FHIR / JSON error bodies (no external calls).
 */
public final class ResponseBodyLeakageAnalyzer {

    private static final Pattern STACK_FRAME = Pattern.compile("(?m)^\\s*at\\s+[a-z0-9_.]+\\.[A-Z][a-zA-Z0-9_$]+\\.[a-z][a-zA-Z0-9_$]+\\(");
    private static final Pattern JAVA_FQN = Pattern.compile("\\bjava\\.lang\\.[A-Za-z0-9_]+Exception\\b");
    private static final Pattern FILE_LINE = Pattern.compile("[A-Za-z]:\\\\[^\\n\"]{8,}|\\.java:\\d+|\\.kt:\\d+");
    private static final Pattern INFRA = Pattern.compile("jdbc:[a-z]+:|postgresql://|mysql://|mongodb://|/var/|/home/|/usr/|Caused by:\\s*java\\.", Pattern.CASE_INSENSITIVE);

    private ResponseBodyLeakageAnalyzer() {}

    public static LeakageExposure analyze(Integer statusCode, String body) {
        if (body == null || body.isBlank()) {
            return LeakageExposure.NONE;
        }
        String s = body;
        int len = s.length();
        String lower = s.toLowerCase(Locale.ROOT);

        if (IMPLEMENTATION_DETAIL_LEAK(lower, s)) {
            return LeakageExposure.IMPLEMENTATION_DETAIL_LEAK;
        }
        if (VERBOSE_ERROR_BODY(statusCode, len, lower)) {
            return LeakageExposure.VERBOSE_ERROR_BODY;
        }
        return LeakageExposure.NONE;
    }

    private static boolean IMPLEMENTATION_DETAIL_LEAK(String lower, String raw) {
        if (STACK_FRAME.matcher(raw).find()) {
            return true;
        }
        if (JAVA_FQN.matcher(raw).find()) {
            return true;
        }
        if (lower.contains("nested exception is:") || lower.contains("exception in thread")) {
            return true;
        }
        if (lower.contains("sqlsyntaxerrorexception") || lower.contains("psqlexception") || lower.contains("sqlgrammar")) {
            return true;
        }
        if (INFRA.matcher(raw).find()) {
            return true;
        }
        if (FILE_LINE.matcher(raw).find()) {
            return true;
        }
        if (lower.contains("springframework") && lower.contains("exception")) {
            return true;
        }
        if (lower.contains("hibernate") && lower.contains("exception")) {
            return true;
        }
        return false;
    }

    private static boolean VERBOSE_ERROR_BODY(Integer statusCode, int len, String lower) {
        if (len >= 12_000) {
            return true;
        }
        if (statusCode != null && statusCode >= 500 && len >= 2_000) {
            return true;
        }
        if (lower.contains("operationoutcome") && len >= 6_000) {
            return true;
        }
        if (lower.contains("\"stacktrace\"") || lower.contains("\"stack_trace\"")) {
            return true;
        }
        return false;
    }

    public static LeakageExposure worst(LeakageExposure a, LeakageExposure b) {
        if (a == null) {
            return b == null ? LeakageExposure.NONE : b;
        }
        if (b == null) {
            return a;
        }
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
