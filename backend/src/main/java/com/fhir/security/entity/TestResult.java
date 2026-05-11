package com.fhir.security.entity;

import com.fhir.security.attack.AttackClassification;
import com.fhir.security.attack.AttackSeverity;
import com.fhir.security.attack.LeakageExposure;
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

    /** Comma-separated stable tags for aggregation (e.g. http.post.patient,fhir.extension.covert_channel). */
    @Column(name = "attack_vector_ids", columnDefinition = "TEXT")
    private String attackVectorIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "leakage_exposure")
    private LeakageExposure leakageExposure;

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

    public String getAttackVectorIds() {
        return attackVectorIds;
    }

    public void setAttackVectorIds(String attackVectorIds) {
        this.attackVectorIds = attackVectorIds;
    }

    public LeakageExposure getLeakageExposure() {
        return leakageExposure;
    }

    public void setLeakageExposure(LeakageExposure leakageExposure) {
        this.leakageExposure = leakageExposure;
    }

    public String getAttackVectorIdsResolved() {
        return attackVectorIds != null ? attackVectorIds : "";
    }

    public LeakageExposure getLeakageExposureResolved() {
        return leakageExposure != null ? leakageExposure : LeakageExposure.NONE;
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
