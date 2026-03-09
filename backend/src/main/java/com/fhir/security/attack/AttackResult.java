package com.fhir.security.attack;

public record AttackResult(int statusCode, String responseBody, boolean vulnerable) {

}