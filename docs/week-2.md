# Week 2 — Summary

## Database & Server Management Layer

**Weekly Goal:** Persist server configurations and support multi-server management.

---

## What Was Achieved

### ✅ PostgreSQL Connected

- PostgreSQL installed and configured
- Database `fhir_security` created
- Dedicated user `fhir_app` with appropriate privileges
- Connection verified via pgAdmin / psql
- Spring Boot datasource configured in `application.properties`

### ✅ JPA Entities Implemented

| Entity | Table | Purpose |
|--------|-------|---------|
| `FhirServer` | `fhir_server` | FHIR server configurations (name, baseUrl, authenticationType) |
| `AttackScenario` | `attack_scenario` | Attack metadata (name, description, severity) |
| `TestRun` | `test_run` | Single test execution against a server |
| `TestResult` | `test_result` | Result of one attack scenario in a run |

**Relationships:**
- `FhirServer` 1 → N `TestRun`
- `TestRun` 1 → N `TestResult`
- `AttackScenario` 1 → N `TestResult`

### ✅ CRUD for FhirServer

- **POST** `/api/servers` — Add server
- **GET** `/api/servers` — List all servers
- **DELETE** `/api/servers/{id}` — Remove server (404 if not found)

### ✅ Angular UI

- `ServerService` with `getServers()`, `addServer()`, `deleteServer()`
- `ServerManagementComponent` with form (name, baseUrl) and table
- Add and Delete buttons
- CORS configured via `WebConfig`
- Frontend and backend fully connected

### ✅ Data Persisted in Database

- Servers added via UI are stored in PostgreSQL
- Hibernate DDL auto-update creates and maintains tables

---

## Technical Highlights

### DTO Layer

- **Request:** `AddServerRequest` (dto.request)
- **Response:** `FhirServerResponse` (dto.response)
- **Mapper:** `FhirServerMapper.toResponse()`
- Entities are never returned directly from controllers

### Validation

- `@NotBlank` on name and baseUrl
- `@URL` on baseUrl (Hibernate Validator)
- `GlobalExceptionHandler` for validation errors, 404, and other exceptions

### Package Structure

```
dto/
├── request/     — AddServerRequest, CreatePatientRequest
├── response/    — FhirServerResponse, PatientDto, ObservationDto, CreatePatientResult
├── ApiError.java
└── TestResult.java
```

### Logging

- Server creation and removal logged in `FhirServerService`
- API requests logged in `FhirServerController`

---

## Deliverables Checklist

| Deliverable | Status |
|-------------|--------|
| PostgreSQL connected | ✔ |
| `fhir_server` table created | ✔ |
| JPA functioning correctly | ✔ |
| AttackScenario, TestRun, TestResult entities | ✔ |
| REST API for server management | ✔ |
| Angular UI adds and lists servers | ✔ |
| DTO layer (no entities in API) | ✔ |
| Validation (@NotBlank, @URL) | ✔ |
| GlobalExceptionHandler | ✔ |
| Clean package structure | ✔ |

---

## Quote

> *"The platform now persists server configurations and supports multi-server management."*

---

## Next Steps (Week 3)

- Implement `AttackScenario` interface (attack engine)
- Implement `AttackExecutorService`
- First attack: Malformed JSON request
- Store and display test results in UI
