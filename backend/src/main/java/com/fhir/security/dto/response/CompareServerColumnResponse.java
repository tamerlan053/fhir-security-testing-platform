package com.fhir.security.dto.response;

/**
 * Metadata for one server column (latest run used for comparison).
 */
public record CompareServerColumnResponse(
        long serverId,
        String serverName,
        String baseUrl,
        Long testRunId,
        String startedAt,
        int vulnerableCount,
        int resultCount
) {}
