package com.fhir.security.dto.response;

/**
 * One cell: how a server responded to an attack in its latest test run.
 */
public record CompareCellResponse(
        long serverId,
        boolean present,
        Integer statusCode,
        Boolean vulnerable
) {}
