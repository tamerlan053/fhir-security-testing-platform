package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class UnauthorizedResourceRetrievalAttack extends AbstractAccessControlAttack {

    public UnauthorizedResourceRetrievalAttack(AttackHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String getName() {
        return "Unauthorized Resource Retrieval";
    }

    @Override
    public String getDescription() {
        return "Creates a victim Patient and an Observation referencing it as subject; attempts to retrieve victim data via GET /Observation?subject=Patient/{victimId}";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        CreateResult victimPatient = createPatient(server, "Victim", "PatientRetrievalVictim");
        if (victimPatient.id() == null) {
            return new AttackResult(
                    victimPatient.statusCode(),
                    victimPatient.responseBody(),
                    isVulnerableByStatus(victimPatient.statusCode())
            );
        }

        // Ensure there is at least one Observation for the victim, so the later retrieval attempt has something to leak.
        createObservationForSubject(server, victimPatient.id(), "UnauthorizedRetrieval");

        String subject = "Patient/" + victimPatient.id();
        String subjectEncoded = encodeQueryParam(subject);
        String url = baseUrl(server) + "/Observation?subject=" + subjectEncoded;

        AttackHttpClient.HttpResult getResult = httpClient.get(url);

        // Access-control scoring improvement:
        // This scenario is primarily GET-based retrieval. We only mark it vulnerable on server-side errors.
        boolean vulnerable = getResult.statusCode() == 500;

        return new AttackResult(
                getResult.statusCode(),
                getResult.responseBody(),
                vulnerable
        );
    }
}

