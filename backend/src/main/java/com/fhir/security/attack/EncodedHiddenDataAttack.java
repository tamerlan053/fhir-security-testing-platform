package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class EncodedHiddenDataAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public EncodedHiddenDataAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Encoded Hidden Data";
    }

    @Override
    public String getDescription() {
        return "Sends Patient meta.tag with Unicode-escaped display text to test encoded covert payload storage";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"Patient\",\"meta\":{\"tag\":[{\"code\":\"x\",\"display\":\"\\u0053\\u0065\\u0063\\u0072\\u0065\\u0074\"}]}}";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 201 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
