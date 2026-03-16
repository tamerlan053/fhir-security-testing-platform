package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;

public interface ExecutableAttack {

    String getName();

    String getDescription();

    AttackResult execute(FhirServer server);
}
