package com.fhir.security.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record TestRunResponse(
        Long id,
        Long serverId,
        String serverName,
        LocalDateTime startedAt,
        List<TestResultResponse> results
) {}
