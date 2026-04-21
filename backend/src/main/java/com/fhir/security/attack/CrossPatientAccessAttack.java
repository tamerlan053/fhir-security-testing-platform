package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEnvironmentProbe;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Cross-patient read: direct Patient/{id} and Observation search by victim subject.
 */
@Component
@Order(90)
public class CrossPatientAccessAttack extends AbstractAccessControlAttack {

    private final AuthEnvironmentProbe authEnvironmentProbe;

    public CrossPatientAccessAttack(AttackHttpClient httpClient, AuthEnvironmentProbe authEnvironmentProbe) {
        super(httpClient);
        this.authEnvironmentProbe = authEnvironmentProbe;
    }

    @Override
    public String getName() {
        return "Cross-Patient Access";
    }

    @Override
    public String getDescription() {
        return "GET Patient/{victimId} and GET Observation?subject=Patient/{victimId} after creating victim resources";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = baseUrl(server);
        boolean oauthAdvertised = authEnvironmentProbe.isOAuthAdvertised(base);

        createPatient(server, "Attacker", "PatientA");

        CreateResult victimPatient = createPatient(server, "Victim", "PatientB");
        if (victimPatient.id() == null) {
            return AttackOutcome.setupCreateFailed(victimPatient.statusCode(), victimPatient.responseBody());
        }

        String patientUrl = base + "/Patient/" + victimPatient.id();
        AttackHttpClient.HttpResult getPatient = httpClient.get(patientUrl);
        AttackResult patientOutcome = AttackOutcome.crossPatientRead(
                getPatient.statusCode(),
                getPatient.responseBody(),
                oauthAdvertised
        );

        createObservationForSubject(server, victimPatient.id(), "CrossPatientProbe");

        String subject = "Patient/" + victimPatient.id();
        String obsUrl = base + "/Observation?subject=" + encodeQueryParam(subject);
        AttackHttpClient.HttpResult getObs = httpClient.get(obsUrl);
        AttackResult obsOutcome = AttackOutcome.crossPatientRead(
                getObs.statusCode(),
                getObs.responseBody(),
                oauthAdvertised
        );

        String combined = "GET Patient → HTTP " + getPatient.statusCode()
                + " | GET Observation?subject → HTTP " + getObs.statusCode();
        return AttackOutcome.combineWorst(patientOutcome, obsOutcome, combined);
    }
}
