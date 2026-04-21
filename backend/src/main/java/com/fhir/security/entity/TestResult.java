package com.fhir.security.entity;

import com.fhir.security.attack.AttackClassification;
import com.fhir.security.attack.AttackSeverity;
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

    @Enumerated(EnumType.STRING)
    private AttackClassification classification;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    private AttackSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

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

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public AttackClassification getClassification() {
        return classification;
    }

    public void setClassification(AttackClassification classification) {
        this.classification = classification;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AttackSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AttackSeverity severity) {
        this.severity = severity;
    }

    /**
     * Rows saved before classification columns existed: derive from {@link #vulnerable}.
     */
    public AttackClassification getClassificationResolved() {
        if (classification != null) {
            return classification;
        }
        return vulnerable ? AttackClassification.VULNERABLE : AttackClassification.SECURE;
    }

    public AttackSeverity getSeverityResolved() {
        if (severity != null) {
            return severity;
        }
        return AttackSeverity.INFO;
    }

    public String getReasonResolved() {
        if (reason != null && !reason.isBlank()) {
            return reason;
        }
        return vulnerable ? "Legacy result: marked vulnerable." : "Legacy result: not classified.";
    }
}
