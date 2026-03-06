# Week 1 — Summary

## FHIR Fundamentals & Backend Connectivity

**Weekly Goal:** Connect to public FHIR servers, fetch and create resources, present basic architecture.

---

## What Was Achieved

### ✅ Connect to Public FHIR Server

- HAPI FHIR client integrated (hapi-fhir-base, hapi-fhir-client, hapi-fhir-structures-r4)
- `FhirClientService` with `connectToServer(baseUrl)` and `testConnection()`
- Dynamic connection via `POST /api/fhir/connect?baseUrl=...`
- Tested against HAPI public server (hapi.fhir.org/baseR4)

### ✅ Fetch Patient and Observation Resources

- **GET** `/api/fhir/Patient?count=10` — Fetch patients with configurable count
- **GET** `/api/fhir/Observation?count=10` — Fetch observations
- **GET** `/api/fhir/Observation?patient={id}&count=50` — Fetch observations by patient
- DTOs: `PatientDto`, `ObservationDto` with mappers
- HAPI FHIR fluent API for search and Bundle parsing

### ✅ Create Patient Resource

- **POST** `/api/fhir/Patient` — Create patient with validation
- `CreatePatientRequest` with givenName, familyName, birthDate, gender
- `CreatePatientResult` with success, patientId, statusCode, validationErrors
- Logging of HTTP status codes and validation errors

### ✅ Architecture Diagram

- Layer definitions in `docs/architecture.md`:
  - Controller Layer
  - Service Layer
  - Attack Engine (future)
  - Persistence Layer (future)
  - Frontend (future)
- Layer interaction overview
- Current vs future status

### ✅ Base Interfaces for Attack Engine

- `AttackScenario` (empty interface)

- `AttackExecutorService` (stub with execute, executeAll)

- `TestResult` (DTO: scenarioId, scenarioName, vulnerabilityFound, severity, message, evidence)

### ✅ Error Handling & Logging

- `GlobalExceptionHandler` for IllegalStateException, IllegalArgumentException, validation errors
- `FhirServerException` for FHIR server errors
- Logging in `FhirClientService` and `FhirTestController`
- `ApiError` for consistent error responses

### ✅ Clean Code & Documentation

- Naming fixes (fromCreateRequest, patientId)
- Removed unused code (FhirConfig bean, Enumeration import)
- Root README with Getting Started
- Backend README with API endpoints and setup

---

## REST API Endpoints (Week 1)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/fhir/connect?baseUrl=...` | Connect to FHIR server |
| GET | `/api/fhir/test` | Test connection |
| GET | `/api/fhir/Patient?count=10` | Fetch patients |
| GET | `/api/fhir/Observation?count=10` | Fetch observations |
| GET | `/api/fhir/Observation?patient={id}&count=50` | Fetch observations by patient |
| POST | `/api/fhir/Patient` | Create patient |

---

## Technical Notes

- **DataSource:** Excluded for Week 1; persistence added in Week 2
- **FHIR:** R4 resources via HAPI FHIR 7.6.1
- **Package structure:** controller, service, config, dto, mapper, entity

---

## Deliverables Checklist

| Deliverable | Status |
|-------------|--------|
| Running Spring Boot application | ✔ |
| Connect to public FHIR server | ✔ |
| Fetch Patient resources | ✔ |
| Fetch Observation resources | ✔ |
| Create Patient (if allowed) | ✔ |
| Architecture diagram / layer definitions | ✔ |
| AttackScenario, AttackExecutorService, TestResult base | ✔ |
| Error handling and logging | ✔ |
| README documentation | ✔ |

---

## Quotes

> *"The system successfully retrieves real FHIR data."*

> *"The project is structured for extensible attack implementation."*

---

## Next Steps (Week 2)

- Add PostgreSQL and JPA
- Create FhirServer entity
- Implement server management CRUD
- Angular UI for server management
