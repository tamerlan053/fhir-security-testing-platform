package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class IdTamperingAttack extends AbstractAccessControlAttack {

    public IdTamperingAttack(AttackHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String getName() {
        return "ID Tampering";
    }

    @Override
    public String getDescription() {
        return "Creates a Patient (victim) and attempts to overwrite it via PUT /Patient/{victimId} (tests ID tampering / insecure update authorization)";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        CreateResult victimPatient = createPatient(server, "Victim", "PatientVictim");
        if (victimPatient.id() == null) {
            return new AttackResult(
                    victimPatient.statusCode(),
                    victimPatient.responseBody(),
                    isVulnerableByStatus(victimPatient.statusCode())
            );
        }

        String idVictim = victimPatient.id();
        String token = safeToken();
        String payload = "{"
                + "\"resourceType\":\"Patient\","
                + "\"id\":\"" + escapeJson(idVictim) + "\","
                + "\"name\":[{\"given\":[\"Tampered-" + token + "\"],\"family\":\"TamperedFamily-" + token + "\"}]"
                + "}";

        String url = baseUrl(server) + "/Patient/" + idVictim;
        AttackHttpClient.HttpResult putResult = httpClient.put(url, payload);

        return new AttackResult(
                putResult.statusCode(),
                putResult.responseBody(),
                isVulnerableByStatus(putResult.statusCode())
        );
    }
}

