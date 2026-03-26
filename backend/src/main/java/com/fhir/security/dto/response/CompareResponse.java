package com.fhir.security.dto.response;

import java.util.List;

public record CompareResponse(
        List<CompareServerColumnResponse> servers,
        List<CompareAttackRowResponse> attacks
) {}
