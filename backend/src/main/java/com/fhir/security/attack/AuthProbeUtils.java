package com.fhir.security.attack;

final class AuthProbeUtils {

    private AuthProbeUtils() {}

    static String normalizeBase(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        return baseUrl.replaceAll("/$", "");
    }

    static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "…";
    }
}
