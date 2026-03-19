package com.fhir.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class AttackHttpClient {

    private static final Logger log = LoggerFactory.getLogger(AttackHttpClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public HttpResult post(String url, String body) {
        try {
            return restTemplate.execute(
                    URI.create(url),
                    HttpMethod.POST,
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        request.getBody().write(body.getBytes(StandardCharsets.UTF_8));
                    },
                    this::extractStatusAndBody
            );
        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : "";
            return new HttpResult(statusCode, responseBody);
        } catch (RestClientException e) {
            log.warn("HTTP request failed: {} - {}", url, e.getMessage());
            return new HttpResult(0, "Error: " + e.getMessage());
        }
    }

    private HttpResult extractStatusAndBody(ClientHttpResponse response) throws IOException {
        int statusCode = response.getStatusCode().value();
        String responseBody = readFully(response.getBody());
        return new HttpResult(statusCode, responseBody != null ? responseBody : "");
    }

    private static String readFully(java.io.InputStream stream) throws IOException {
        if (stream == null) return "";
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             java.io.BufferedReader br = new java.io.BufferedReader(reader)) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }а

    public record HttpResult(int statusCode, String responseBody) {}
}
