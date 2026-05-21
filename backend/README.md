# FHIR Security Testing Platform — Backend

Spring Boot backend for the FHIR Security Testing Platform.

## Prerequisites

- Java 21
- Maven 3.6+

## Build & Run

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

The server starts on **http://localhost:8080**.

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `fhir.server.url` | `http://hapi.fhir.org/baseR4` | Reference default; connection is set via `POST /connect` |
| `server.port` | `8080` | HTTP port |
| `fhir.security.test.bearer-token` | _(empty)_ | Optional lab OAuth bearer (use env `FHIR_SECURITY_TEST_BEARER_TOKEN`; Week 11 isolation probe) |
| `fhir.security.test.out-of-scope-patient-id` | _(empty)_ | Patient logical id the token must not read (`FHIR_SECURITY_TEST_OUT_OF_SCOPE_PATIENT_ID`) |
| `fhir.security.test.lab-base-url-contains` | _(empty)_ | If set, token probe runs only when server `baseUrl` contains this substring (`FHIR_SECURITY_TEST_LAB_BASE_CONTAINS`) |

## API Endpoints

### FHIR connection & resources

Base path: `/api/fhir`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/connect?baseUrl=<url>` | Connect to a FHIR server |
| GET | `/test` | Test current connection |
| GET | `/Patient?count=<n>` | Fetch patients (default count: 10) |
| GET | `/Observation?count=<n>` | Fetch observations (default count: 10) |
| GET | `/Observation?patient=<id>&count=<n>` | Fetch observations for a patient |
| POST | `/Patient` | Create a patient (JSON body) |

### Example: Connect and Fetch

```bash
# Connect to HAPI public server
curl -X POST "http://localhost:8080/api/fhir/connect?baseUrl=http://hapi.fhir.org/baseR4"

# Test connection
curl http://localhost:8080/api/fhir/test

# Fetch 5 patients
curl "http://localhost:8080/api/fhir/Patient?count=5"
```

## Project Structure

```
src/main/java/com/fhir/security/
├── attack/          # Attack engine (AttackScenario)
├── config/          # Spring configuration
├── controller/      # REST controllers
├── dto/             # Data transfer objects
├── mapper/          # FHIR ↔ DTO mappers
└── service/         # Business logic (FhirClientService, AttackExecutorService)
```
