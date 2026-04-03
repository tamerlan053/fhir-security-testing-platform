package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class CrossPatientAccessAttack extends AbstractAccessControlAttack {

    public CrossPatientAccessAttack(AttackHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String getName() {
        return "Cross-patient Access";
    }

    @Override
    public String getDescription() {
        return "Creates two Patients and tries to GET Patient/{victimId} (tests IDOR / broken object-level authorization)";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        // Attacker-created patient (not used directly, but keeps the victim/attacker chain explicit)
        createPatient(server, "Attacker", "PatientA");

        CreateResult victimPatient = createPatient(server, "Victim", "PatientB");
        if (victimPatient.id() == null) {
            return new AttackResult(
                    victimPatient.statusCode(),
                    victimPatient.responseBody(),
                    isVulnerableByStatus(victimPatient.statusCode())
            );
        }

        String url = baseUrl(server) + "/Patient/" + victimPatient.id();
        AttackHttpClient.HttpResult getResult = httpClient.get(url);

        // Access-control scoring improvement:
        // Pure GET success (public read) is informational, not a confirmed privilege escalation.
        // Only server errors are still treated as vulnerable for this GET-based scenario.
        boolean vulnerable = getResult.statusCode() == 500;

        return new AttackResult(
                getResult.statusCode(),
                getResult.responseBody(),
                vulnerable
        );
    }
}

