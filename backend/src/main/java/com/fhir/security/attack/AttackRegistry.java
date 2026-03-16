package com.fhir.security.attack;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AttackRegistry {

    private final List<ExecutableAttack> scenarios;

    public AttackRegistry(List<ExecutableAttack> scenarios) {
        this.scenarios = scenarios;
    }

    public List<ExecutableAttack> getScenarios() {
        return scenarios;
    }
}
