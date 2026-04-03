# Week 6 - Summary

## Resource Manipulation & Access Testing

**Weekly Goal:** Detect improper resource access controls (IDOR-style reads, reference/owner manipulation, ID tampering updates, and unauthorized retrieval paths) and surface results in the UI and report.

---

## What Was Achieved

### ✅ Four Access-Control Attacks Implemented

Each scenario is implemented as a multi-step `ExecutableAttack` and executed automatically via `AttackRegistry` + `AttackExecutorService`.

| Attack | Purpose | Key endpoint(s) |
|--------|---------|------------------|
| **Cross-patient Access** | Attempt to read another patient by ID (IDOR-style read) | `GET /Patient/{victimId}` |
| **Owner/Reference Manipulation** | Create a resource that references a victim as `subject`/owner | `POST /Observation` with `subject=Patient/{victimId}` |
| **ID Tampering** | Attempt to overwrite victim patient | `PUT /Patient/{victimId}` |
| **Unauthorized Resource Retrieval** | Attempt to retrieve victim-linked resources via search | `GET /Observation?subject=Patient/{victimId}` |

### ✅ Backend Runner Improvements

- Added `GET` and `PUT` support to `AttackHttpClient` (Week 6 required more than POST-only).
- Added `FhirResourceIdExtractor` to extract created resource IDs from response bodies for multi-step chains.
- Added a shared base class for access-control attacks to reduce duplication and keep payload/ID handling consistent.

---

## Vulnerability Evaluation (Week 6)

Access-control scoring is **method-aware** to avoid false positives from public GET reads:

- GET-based scenarios are treated as **informational on success**:
  - `Cross-patient Access`: `vulnerable = (statusCode == 500)`
  - `Unauthorized Resource Retrieval`: `vulnerable = (statusCode == 500)`
- Write-based scenarios use the project’s “accepted or server error” rule:
  - `Owner/Reference Manipulation` (POST step): `vulnerable = (statusCode == 200 || statusCode == 201 || statusCode == 500)`
  - `ID Tampering` (PUT step): `vulnerable = (statusCode == 200 || statusCode == 201 || statusCode == 500)`

---

## Server Results (Recorded Runs)

Values below were collected by running the suite in the UI and reading back saved `statusCode` / `vulnerable` via `GET /api/results/{testRunId}`.

| Server | Cross-patient Access | Owner/Reference Manipulation | ID Tampering | Unauthorized Resource Retrieval |
|--------|------------------------|--------------------------------|--------------|----------------------------------|
| HAPI Public (`http://hapi.fhir.org/baseR4`, testRunId=23) | 200 / vulnerable=False | 201 / vulnerable=True | 200 / vulnerable=True | 200 / vulnerable=False |
| Firely (`https://server.fire.ly/r4`, testRunId=24) | 200 / vulnerable=False | 201 / vulnerable=True | 200 / vulnerable=True | 200 / vulnerable=False |
| Smarthealthit (`https://r4.smarthealthit.org`, testRunId=25) | 200 / vulnerable=False | 201 / vulnerable=True | 200 / vulnerable=True | 200 / vulnerable=False |

### Key Takeaway

Across tested servers, write-based privilege escalation paths (reference manipulation via POST and overwrite attempts via PUT) were accepted and flagged vulnerable, while pure GET reads were treated as informational under the improved Week 6 scoring.

---

## Technical Highlights

### No New Attack Orchestration Endpoints

- Same flow as prior weeks: `POST /api/attacks/run/{serverId}`, `GET /api/results/{testRunId}`
- New `@Component` beans are picked up automatically by `AttackRegistry`
- Week 6 required broader HTTP support in `AttackHttpClient` (GET/PUT)

### Access-Control Scenario Pattern (Multi-step)

- Create victim resources (`Patient`, then optionally `Observation`)
- Extract created resource IDs from response bodies for follow-up requests
- Attempt read/search/update actions that should be restricted
- Persist `statusCode`, `responseBody`, and `vulnerable` in `TestResult`

### Angular UI Updates

- **Overall summary:** `X of Y attacks vulnerable`
- **Access-control summary:** `X of Y access-control attacks vulnerable`
- Results table unchanged: Attack | Status Code | Vulnerable (✓ OK / ⚠ Vulnerable)

---

## Why This Matters (Privilege Escalation Risk)

- Successful unauthorized updates can corrupt patient records (integrity impact).
- Successful reference/ownership manipulation can create pivot paths to victim-linked resources.
- Even when reads are public, write acceptance indicates weak enforcement for resource integrity.

---

## Deliverables Checklist

| Deliverable | Status |
|-------------|--------|
| Cross-patient access attack | ✔ |
| Owner/reference manipulation attack | ✔ |
| ID tampering attack | ✔ |
| Unauthorized resource retrieval attack | ✔ |
| GET/PUT support in AttackHttpClient | ✔ |
| Week 6 access-control summary in UI | ✔ |
| Verified results on 2–3 servers | ✔ |
| Week 6 report updated with recorded runs | ✔ |

---

## Quote

> *"Some servers allow improper resource access."*

---

## Next Steps (Week 7)

- Authentication & authorization testing (detect open endpoints, Basic Auth, OAuth2/SMART simulation)
- Token misuse attempts (invalid/expired tokens, tampered headers)
- Classify authentication strategies per server and document weak or missing enforcement

