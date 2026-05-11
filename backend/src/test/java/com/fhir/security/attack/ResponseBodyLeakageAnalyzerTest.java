package com.fhir.security.attack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseBodyLeakageAnalyzerTest {

    @Test
    void detects_stack_like_content() {
        String body = "{\"diagnostic\":\"java.lang.NullPointerException\\n\\tat com.example.Foo.parse(Foo.java:42)\"}";
        assertEquals(LeakageExposure.IMPLEMENTATION_DETAIL_LEAK, ResponseBodyLeakageAnalyzer.analyze(500, body));
    }

    @Test
    void none_for_short_operation_outcome() {
        assertEquals(LeakageExposure.NONE, ResponseBodyLeakageAnalyzer.analyze(400,
                "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"invalid\"}]}"));
    }

    @Test
    void verbose_for_very_large_body() {
        String body = "x".repeat(13000);
        assertEquals(LeakageExposure.VERBOSE_ERROR_BODY, ResponseBodyLeakageAnalyzer.analyze(400, body));
    }

    @Test
    void worst_prefers_implementation_over_verbose() {
        assertEquals(LeakageExposure.IMPLEMENTATION_DETAIL_LEAK,
                ResponseBodyLeakageAnalyzer.worst(LeakageExposure.VERBOSE_ERROR_BODY, LeakageExposure.IMPLEMENTATION_DETAIL_LEAK));
    }
}
