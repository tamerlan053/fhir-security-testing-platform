package com.fhir.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AttackHttpClient {

    private static final Logger log = LoggerFactory.getLogger(AttackHttpClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public HttpResult post(String url, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            int statusCode = response.getStatusCode().value();
            String responseBody = response.getBody() != null ? response.getBody() : "";
            return new HttpResult(statusCode, responseBody);
        } catch (RestClientException e) {
            log.warn("HTTP request failed: {} - {}", url, e.getMessage());
            return new HttpResult(0, "Error: " + e.getMessage());
        }
    }

    public record HttpResult(int statusCode, String responseBody) {}
}
