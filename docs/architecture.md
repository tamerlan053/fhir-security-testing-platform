# FHIR Security Testing Platform — Architecture

## Project Architecture — Layer Definitions

### 1. **Controller Layer** (REST API)

**Role:** HTTP boundary that receives requests, validates input, and returns responses.

| Aspect | Definition |
|--------|------------|
| **Responsibility** | Map HTTP requests to service calls, handle status codes, and serialize DTOs. No business logic. |
| **Technology** | Spring Boot `@RestController` |
| **Current** | `FhirTestController` — connect, test, Patient/Observation CRUD |
| **Planned** | Server management, attack execution, report endpoints |

**Typical responsibilities:**

- Connection: `/api/fhir/connect`, `/api/fhir/test`
- FHIR: `/api/fhir/Patient`, `/api/fhir/Observation`
- Server management (future): add, remove, list servers
- Attack execution (future): trigger scenarios, get results
- Reports (future): fetch/export PDF, JSON, CSV

**Principles:** Thin controllers, DTOs only, delegating to the service layer.

---

### 2. **Service Layer** (Business Logic)

**Role:** Business logic and orchestration between controllers, attack engine, persistence, and external FHIR servers.

| Aspect | Definition |
|--------|------------|
| **Responsibility** | Connect to FHIR servers, coordinate attack runs, compute scores, prepare reports. |
| **Technology** | Spring `@Service` |
| **Current** | `FhirClientService` — HAPI FHIR connectivity (Patient, Observation, create) |
| **Planned** | `AttackExecutorService`, report generation, server management |

**Service components (existing + planned):**

| Service | Responsibility |
|---------|----------------|
| `FhirClientService` ✓ | Raw FHIR operations (connect, read, create) |
| `AttackExecutorService` (future) | Run scenarios via Attack Engine, store results |
| `ServerManagementService` (future) | CRUD for `FhirServer` configurations |
| `ReportService` (future) | Build reports, export PDF/JSON/CSV |
| `ScoringService` (future) | Calculate vulnerability scores from results |

**Principles:** Stateless services, no HTTP logic, clear separation from controllers and persistence.

---

### 3. **Attack Engine** (future)

**Role:** Pluggable security testing framework for FHIR APIs.

| Aspect | Definition |
|--------|------------|
| **Responsibility** | Run attack scenarios, record outcomes, expose generic execution contract. |
| **Technology** | Java interfaces and implementations |
| **Pattern** | Strategy/Plugin — each attack is a separate `AttackScenario` implementation |

**Intended structure:**

```
AttackScenario (interface)
├── execute(FhirServer, context) → TestResult
├── getId(), getName(), getCategory()
└── implementations:
    ├── MalformedJsonAttack
    ├── HiddenDataInjectionAttack
    ├── CrossPatientAccessAttack
    ├── AuthWeaknessAttack
    ├── DataLeakageAttack
    └── ...
```

**Attack categories (from README):**

- Malformed request testing (invalid JSON, duplicate fields, broken metadata)
- Covert channels (extensions, identifiers, contained resources)
- Access control (cross-patient, ID tampering)
- Auth/authz (open endpoints, OAuth2, token misuse)
- Data leakage (verbose errors, internal IDs)

**Principles:** Single responsibility per attack, no persistence or HTTP in the engine itself.

---

### 4. **Persistence Layer** (future)

**Role:** Persist configuration and test results in PostgreSQL.

| Aspect | Definition |
|--------|------------|
| **Responsibility** | Store FHIR server configs, scenarios, runs, and results. |
| **Technology** | Spring Data JPA / Hibernate, PostgreSQL |

**JPA entities (from README):**

| Entity | Purpose |
|--------|---------|
| `FhirServer` | Base URL, name, credentials (optional) |
| `AttackScenario` | Metadata for each scenario (ID, name, category) |
| `TestRun` | Timestamp, server, scenario(s), status |
| `TestResult` | Outcome of a single scenario: severity, evidence, details |

**Planned access pattern:**

- Controller → Service → Repository → DB
- Attack Engine → returns `TestResult` → Service persists via repository

**Principles:** Entities and repositories only; no business logic in repositories.

---

### 5. **Frontend** (future)

**Role:** Angular SPA for managing servers, running tests, and viewing reports.

| Aspect | Definition |
|--------|------------|
| **Responsibility** | UI for server management, attack runs, results, and reports. |
| **Technology** | Angular |

**Planned views:**

| View | Purpose |
|------|---------|
| Server management | Add/remove/list FHIR servers |
| Dashboard | Security overview, charts, severity breakdown |
| Attack execution | Select server + scenarios → run → show results |
| History | Past test runs and outcomes |
| Reports | View and export (PDF, JSON, CSV) |

**Data flow:** HTTP calls to Controller Layer APIs; services/components for state and display.

---

## Layer Interaction Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Frontend (Angular)                                         │
│  Dashboard · Server Mgmt · Attack Runs · Reports             │
└─────────────────────────────────┬───────────────────────────┘
                                  │ HTTP/REST
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│  Controller Layer (REST)                                    │
│  FhirTestController · ServerController · AttackController   │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────┐
│  Service Layer                                              │
│  FhirClientService · AttackExecutorService · ReportService  │
└────────────┬──────────────────────────────┬────────────────┘
             │                               │
             ▼                               ▼
┌──────────────────────┐        ┌─────────────────────────────┐
│  Attack Engine       │        │  Persistence Layer          │
│  AttackScenario(s)   │        │  FhirServer · TestRun       │
│  → TestResult        │        │  TestResult · JPA Repos     │
└──────────┬───────────┘        └──────────────┬──────────────┘
           │                                   │
           │  HTTP                             │  SQL
           ▼                                   ▼
┌──────────────────────┐        ┌─────────────────────────────┐
│  External FHIR        │        │  PostgreSQL                 │
│  Servers              │        │                             │
└──────────────────────┘        └─────────────────────────────┘
```

---

## Current vs Future Summary

| Layer | Status | Notes |
|-------|--------|-------|
| Controller | ✓ Implemented | `FhirTestController` exists |
| Service | ✓ Partial | `FhirClientService` only; other services planned |
| Attack Engine | Future | `AttackScenario` and `AttackExecutorService` to be added |
| Persistence | Future | JPA/PostgreSQL excluded for now; entities planned |
| Frontend | Future | Angular app not started yet |
