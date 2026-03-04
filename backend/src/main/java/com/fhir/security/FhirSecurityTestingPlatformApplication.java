package com.fhir.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class FhirSecurityTestingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(FhirSecurityTestingPlatformApplication.class, args);
	}

}
