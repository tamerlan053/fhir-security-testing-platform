package com.fhir.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class FhirSecurityTestingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(FhirSecurityTestingPlatformApplication.class, args);
	}

}
