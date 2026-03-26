package com.fhir.security.dto.response;

import java.util.List;

public record CompareAttackRowResponse(
        String scenarioName,
        List<CompareCellResponse> cells
) {}
