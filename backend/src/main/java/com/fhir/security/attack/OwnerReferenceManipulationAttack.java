package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class OwnerReferenceManipulationAttack extends AbstractAccessControlAttack {

    public OwnerReferenceManipulationAttack(AttackHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String getName() {
        return "Owner/Reference Manipulation";
    }

    @Override
    public String getDescription() {
        return "Creates a Patient (victim) and then creates an Observation referencing it as subject; tries to GET the Observation (tests reference/ownership validation)";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        CreateResult victimPatient = createPatient(server, "Victim", "PatientOwner");
        if (victimPatient.id() == null) {
            return new AttackResult(
                    victimPatient.statusCode(),
                    victimPatient.responseBody(),
                    isVulnerableByStatus(victimPatient.statusCode())
            );
        }

        CreateResult observation = createObservationForSubject(server, victimPatient.id(), "OwnerRefManipulation");
        if (observation.id() == null) {
            boolean vulnerableFromPost = isVulnerableByStatus(observation.statusCode());
            return new AttackResult(
                    observation.statusCode(),
                    observation.responseBody(),
                    vulnerableFromPost
            );
        }

        // Access-control scoring improvement:
        // Reference manipulation is the attacker-controlled POST (Observation.subject pointing to the victim).
        // If that POST succeeds (200/201/500), we treat it as vulnerable even if the later GET outcome is informational.
        boolean vulnerableFromPost = isVulnerableByStatus(observation.statusCode());

        String url = baseUrl(server) + "/Observation/" + observation.id();
        AttackHttpClient.HttpResult getResult = httpClient.get(url);

        String combinedBody = ""
                + "POST /Observation responseBody (scoring basis):\n"
                + (observation.responseBody() != null ? observation.responseBody() : "")
                + "\n\nGET /Observation/{id} responseBody (extra confirmation):\n"
                + (getResult.responseBody() != null ? getResult.responseBody() : "");

        // Store POST statusCode as the single statusCode for the scenario, since it's the "dangerous" action.
        return new AttackResult(observation.statusCode(), combinedBody, vulnerableFromPost);
    }
}

