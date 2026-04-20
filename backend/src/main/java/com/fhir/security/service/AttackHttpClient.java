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
import java.util.stream.Collectors;

@Service
public class AttackHttpClient {

    private static final Logger log = LoggerFactory.getLogger(AttackHttpClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

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
        return execute(url, HttpMethod.GET, headers, request -> {
            // No request body
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
                request -> {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    if (body != null) {
                        request.getBody().write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }
        );
    }

    private HttpResult execute(String url, HttpMethod method, HttpHeaders additionalHeaders, RequestCallback requestCallback) {
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

    public record HttpResult(int statusCode, String responseBody) {}
}
