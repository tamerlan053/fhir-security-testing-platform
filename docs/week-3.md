# Week 3 — Summary

## Attack Framework Architecture

**Weekly Goal:** Implement extensible attack architecture, execute automated attacks, store results in the database, and display them in the Angular UI.

---

## What Was Achieved

### ✅ Extensible Attack Architecture

- **ExecutableAttack** (interface) — contract for pluggable attack modules
  - `getName()`, `getDescription()`, `execute(FhirServer server)`
  - Renamed from `AttackScenario` to resolve conflict with JPA entity
- **AttackResult** (record) — `statusCode`, `responseBody`, `vulnerable`
- **AttackRegistry** — collects all `ExecutableAttack` implementations via Spring injection
- **AttackExecutorService** — orchestrates attack execution and persistence

### ✅ Attack Execution Flow

```
UI (Angular)
    ↓
AttackController
    ↓
AttackExecutorService.executeAll(server)
    ↓
AttackRegistry.getScenarios() → ExecutableAttack
    ↓
AttackHttpClient → FHIR Server
    ↓
TestResult (entity) → PostgreSQL
```

### ✅ First Attack: Malformed JSON

- **MalformedJsonAttack** — sends truncated JSON to `/Patient` endpoint
- Tests server validation: 400 = correct, 200 or 500 = vulnerable
- Uses `AttackHttpClient` (RestTemplate) for raw HTTP POST requests

### ✅ REST API for Attacks

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attacks/run/{serverId}` | Run all attacks against a server |
| GET | `/api/attacks/runs/{serverId}` | List test runs for a server |
| GET | `/api/results/{testRunId}` | Get results of a specific run |

### ✅ Results Stored in Database

- **TestRun** — created per execution, linked to server
- **TestResult** — per attack scenario: statusCode, responseBody, vulnerable
- **entity.AttackScenario** — metadata persisted or auto-created by name
- DTOs: `TestRunResponse`, `TestResultResponse`, `RunResultResponse`, `TestRunSummaryResponse`

### ✅ Angular Attack Runner UI

- **AttackService** — `runAttacks()`, `getResults()`, `getRunsForServer()`
- **AttackRunnerComponent** — server selector, "Run Security Test" button, results table
- Table columns: Attack | Status Code | Vulnerable (✓ OK / ⚠ Vulnerable)
- **Route:** `/attacks` with "Run Test" links from server management
- **Change detection:** `ChangeDetectorRef.detectChanges()` for Angular 21 zoneless mode

### ✅ Error Handling & Refactoring

- **TestRunNotFoundException** — returns 404 for missing test run
- **Generic Exception handler** — catches unhandled exceptions
- **Environment config** — `apiBaseUrl` in `environment.ts` (dev/production)
- **formatApiError** — shared utility for API error display
- **ConnectRequest** — `POST /api/fhir/connect` now accepts JSON body

---

## REST API Endpoints (Week 3)

- **POST** `/api/attacks/run/{serverId}` — Run attacks (returns `testRunId`, `startedAt`)
- **GET** `/api/attacks/runs/{serverId}` — List runs for server
- **GET** `/api/results/{testRunId}` — Get run results with scenario details

---

## Technical Highlights

### Package Structure

```
attack/
├── ExecutableAttack.java (interface)
├── AttackResult.java (record)
├── AttackRegistry.java
└── MalformedJsonAttack.java

service/
├── AttackExecutorService.java
└── AttackHttpClient.java

controller/
├── AttackController.java
└── TestResultController.java
```

### Vulnerability Evaluation Logic

| Status Code | Meaning |
|-------------|---------|
| 400 | Correct validation |
| 200 | Bad validation (accepts malformed data) |
| 500 | Server crash |

### Frontend

- **Models:** `TestRun`, `TestResult`, `RunResult`, `TestRunSummary`
- **Environment:** `environment.ts`, `environment.development.ts`, `environment.production.ts`
- **Utils:** `formatApiError()` in `utils/error.utils.ts`

---

## Deliverables Checklist

| Deliverable | Status |
|-------------|--------|
| Extensible attack architecture | ✔ |
| ExecutableAttack interface | ✔ |
| AttackExecutorService working | ✔ |
| First attack (Malformed JSON) implemented | ✔ |
| HTTP responses logged and stored | ✔ |
| Results visible in Angular UI | ✔ |
| POST /api/attacks/run/{serverId} | ✔ |
| GET /api/results/{testRunId} | ✔ |
| GET /api/attacks/runs/{serverId} | ✔ |
| TestRun and TestResult linked | ✔ |

---

## Quote

> *"The platform executes automated attack scenarios."*

---

## Next Steps (Week 4)

- Implement additional attacks (e.g. HiddenDataInjection, CrossPatientAccess)
- Add severity levels to attack results
- Add report generation (JSON/CSV export)
- Improve error handling for validation errors in UI
