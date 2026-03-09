package com.fhir.security.attack;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AttackRegistry {

    private final List<AttackScenario> scenarios;

    public AttackRegistry(List<AttackScenario> scenarios) {
        this.scenarios = scenarios;
    }

    public List<AttackScenario> getScenarios() {
        return scenarios;
    }
}
