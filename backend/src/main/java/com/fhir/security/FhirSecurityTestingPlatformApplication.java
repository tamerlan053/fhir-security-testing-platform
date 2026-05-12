package com.fhir.security;

import com.fhir.security.config.FhirSecurityTestProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FhirSecurityTestProperties.class)
public class FhirSecurityTestingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(FhirSecurityTestingPlatformApplication.class, args);
	}

}
