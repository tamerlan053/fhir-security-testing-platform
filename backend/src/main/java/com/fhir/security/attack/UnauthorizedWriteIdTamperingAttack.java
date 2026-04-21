package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEnvironmentProbe;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Unauthorized modification: PUT Patient tampering and Observation create + read (reference manipulation).
 */
@Component
@Order(100)
public class UnauthorizedWriteIdTamperingAttack extends AbstractAccessControlAttack {

    private final AuthEnvironmentProbe authEnvironmentProbe;

    public UnauthorizedWriteIdTamperingAttack(AttackHttpClient httpClient, AuthEnvironmentProbe authEnvironmentProbe) {
        super(httpClient);
        this.authEnvironmentProbe = authEnvironmentProbe;
    }

    @Override
    public String getName() {
        return "Unauthorized Write / ID Tampering";
    }

    @Override
    public String getDescription() {
        return "PUT /Patient/{id} tampering and POST /Observation with subject reference plus follow-up GET";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        AttackResult idTamper = runIdTampering(server);
        AttackResult ownerRef = runOwnerReferenceObservation(server);
        return AttackOutcome.combineWorst(idTamper, ownerRef, idTamper.responseBody() + "\n\n" + ownerRef.responseBody());
    }

    private AttackResult runIdTampering(FhirServer server) {
        String base = baseUrl(server);
        boolean oauthAdvertised = authEnvironmentProbe.isOAuthAdvertised(base);

        CreateResult victimPatient = createPatient(server, "Victim", "PatientVictim");
        if (victimPatient.id() == null) {
            return AttackOutcome.setupCreateFailed(victimPatient.statusCode(), victimPatient.responseBody());
        }

        String idVictim = victimPatient.id();
        String token = safeToken();
        String payload = "{"
                + "\"resourceType\":\"Patient\","
                + "\"id\":\"" + escapeJson(idVictim) + "\","
                + "\"name\":[{\"given\":[\"Tampered-" + token + "\"],\"family\":\"TamperedFamily-" + token + "\"}]"
                + "}";

        String url = base + "/Patient/" + idVictim;
        AttackHttpClient.HttpResult putResult = httpClient.put(url, payload);
        return AttackOutcome.anonymousWrite(putResult.statusCode(), putResult.responseBody(), oauthAdvertised);
    }

    private AttackResult runOwnerReferenceObservation(FhirServer server) {
        String base = baseUrl(server);
        boolean oauthAdvertised = authEnvironmentProbe.isOAuthAdvertised(base);

        CreateResult victimPatient = createPatient(server, "Victim", "PatientOwnerRef");
        if (victimPatient.id() == null) {
            return AttackOutcome.setupCreateFailed(victimPatient.statusCode(), victimPatient.responseBody());
        }

        CreateResult observation = createObservationForSubject(server, victimPatient.id(), "OwnerRefProbe");
        AttackResult postOutcome = AttackOutcome.anonymousWrite(
                observation.statusCode(),
                observation.responseBody(),
                oauthAdvertised
        );

        if (observation.id() == null) {
            return postOutcome;
        }

        String url = base + "/Observation/" + observation.id();
        AttackHttpClient.HttpResult getResult = httpClient.get(url);

        String combinedBody = ""
                + "POST /Observation:\n"
                + (observation.responseBody() != null ? observation.responseBody() : "")
                + "\n\nGET /Observation/{id}:\n"
                + (getResult.responseBody() != null ? getResult.responseBody() : "");

        String extra = " Follow-up GET HTTP " + getResult.statusCode() + ".";
        return new AttackResult(
                observation.statusCode(),
                combinedBody,
                postOutcome.classification(),
                postOutcome.reason() + extra,
                postOutcome.severity()
        );
    }
}
