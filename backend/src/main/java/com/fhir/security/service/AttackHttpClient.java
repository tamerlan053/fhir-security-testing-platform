package com.fhir.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttackHttpClient {

    private static final Logger log = LoggerFactory.getLogger(AttackHttpClient.class);
    private static final int REQUEST_BODY_LOG_LIMIT = 12_000;
    private static final MediaType FHIR_JSON = MediaType.parseMediaType("application/fhir+json");

    private final RestTemplate restTemplate = new RestTemplate();
    private final ThreadLocal<List<String>> requestTrace = ThreadLocal.withInitial(ArrayList::new);

    /** Clears captured requests before running a single attack scenario. */
    public void clearRequestTrace() {
        requestTrace.get().clear();
    }

    /** Formatted HTTP request log for the current scenario (empty if none were captured). */
    public String getRequestTrace() {
        List<String> entries = requestTrace.get();
        if (entries.isEmpty()) {
            return "";
        }
        return String.join("\n\n---\n\n", entries);
    }

    public HttpResult post(String url, String body) {
        return post(url, body, null);
    }

    /**
     * POST with optional extra headers (e.g. {@code Authorization}) merged before the body is written.
     */
    public HttpResult post(String url, String body, HttpHeaders headers) {
        return execute(
                url,
                HttpMethod.POST,
                headers,
                body,
                MediaType.APPLICATION_JSON,
                request -> {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    request.getBody().write(body.getBytes(StandardCharsets.UTF_8));
                }
        );
    }

    public HttpResult get(String url) {
        return get(url, null);
    }

    /**
     * GET with optional extra headers (e.g. {@code Authorization: Bearer …}).
     */
    public HttpResult get(String url, HttpHeaders headers) {
        return execute(url, HttpMethod.GET, headers, null, null, request -> {
            request.getHeaders().setAccept(List.of(FHIR_JSON));
        });
    }

    public HttpResult put(String url, String body) {
        return put(url, body, null);
    }

    /**
     * PUT with optional extra headers.
     */
    public HttpResult put(String url, String body, HttpHeaders headers) {
        return execute(
                url,
                HttpMethod.PUT,
                headers,
                body,
                MediaType.APPLICATION_JSON,
                request -> {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    if (body != null) {
                        request.getBody().write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }
        );
    }

    private HttpResult execute(
            String url,
            HttpMethod method,
            HttpHeaders additionalHeaders,
            String requestBody,
            MediaType contentType,
            RequestCallback requestCallback
    ) {
        recordRequest(method, url, additionalHeaders, requestBody, contentType);
        try {
            return restTemplate.execute(
                    URI.create(url),
                    method,
                    request -> {
                        if (additionalHeaders != null) {
                            request.getHeaders().putAll(additionalHeaders);
                        }
                        requestCallback.apply(request);
                    },
                    this::extractStatusAndBody
            );
        } catch (RestClientResponseException e) {
            return toHttpResult(e);
        } catch (RestClientException e) {
            log.warn("HTTP failed: {} - {}", url, e.getMessage());
            return new HttpResult(0, "Error: " + e.getMessage());
        }
    }

    private static HttpResult toHttpResult(RestClientResponseException e) {
        int statusCode = e.getStatusCode().value();
        String responseBody = e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : "";
        return new HttpResult(statusCode, responseBody);
    }

    @FunctionalInterface
    private interface RequestCallback {
        void apply(ClientHttpRequest request) throws IOException;
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
    }

    /**
     * POST with {@code application/x-www-form-urlencoded} body (OAuth token endpoint probes).
     */
    public HttpResult postUrlEncoded(String url, String formBody, HttpHeaders additionalHeaders) {
        recordRequest(HttpMethod.POST, url, additionalHeaders, formBody, MediaType.APPLICATION_FORM_URLENCODED);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if (additionalHeaders != null) {
                headers.putAll(additionalHeaders);
            }
            HttpEntity<String> entity = new HttpEntity<>(formBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return new HttpResult(
                    response.getStatusCode().value(),
                    response.getBody() != null ? response.getBody() : ""
            );
        } catch (RestClientResponseException e) {
            return toHttpResult(e);
        } catch (RestClientException e) {
            log.warn("HTTP failed: {} - {}", url, e.getMessage());
            return new HttpResult(0, "Error: " + e.getMessage());
        }
    }

    private void recordRequest(
            HttpMethod method,
            String url,
            HttpHeaders additionalHeaders,
            String requestBody,
            MediaType contentType
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.name()).append(' ').append(url).append('\n');
        if (method == HttpMethod.GET) {
            sb.append("Accept: ").append(FHIR_JSON).append('\n');
        }
        if (contentType != null) {
            sb.append("Content-Type: ").append(contentType).append('\n');
        }
        if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
            additionalHeaders.forEach((name, values) -> {
                for (String value : values) {
                    sb.append(name).append(": ").append(value).append('\n');
                }
            });
        }
        if (requestBody != null && !requestBody.isBlank()) {
            sb.append('\n').append(truncateForLog(requestBody));
        }
        requestTrace.get().add(sb.toString().stripTrailing());
    }

    private static String truncateForLog(String text) {
        if (text.length() <= REQUEST_BODY_LOG_LIMIT) {
            return text;
        }
        return text.substring(0, REQUEST_BODY_LOG_LIMIT) + "\n… [truncated]";
    }

    public record HttpResult(int statusCode, String responseBody) {}
}
