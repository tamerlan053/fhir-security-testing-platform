package com.fhir.security.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test_result")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private AttackScenario scenario;

    private int statusCode;
    private boolean vulnerable;

    public TestResult() {}

    public TestResult(TestRun testRun, AttackScenario scenario, int statusCode, boolean vulnerable) {
        this.testRun = testRun;
        this.scenario = scenario;
        this.statusCode = statusCode;
        this.vulnerable = vulnerable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestRun getTestRun() {
        return testRun;
    }

    public void setTestRun(TestRun testRun) {
        this.testRun = testRun;
    }

    public AttackScenario getScenario() {
        return scenario;
    }

    public void setScenario(AttackScenario scenario) {
        this.scenario = scenario;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }
}
