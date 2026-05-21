# FHIR Security Testing Platform

A full-stack security testing platform for systematically evaluating public FHIR servers for vulnerabilities, authentication weaknesses, and covert data channels.

**Internship Project** · 12-Week Development Plan

---

## Overview

This project delivers a security research platform capable of automated vulnerability assessment against FHIR (Fast Healthcare Interoperability Resources) REST APIs. The system combines a Spring Boot backend with an Angular frontend to provide:

- **Automated attack engine** — Extensible framework for security testing scenarios  
- **Authentication & authorization analysis** — Evaluation of auth mechanisms across servers  
- **Covert channel detection** — Identification of hidden data injection opportunities  
- **Data leakage detection** — Analysis of excessive information exposure  
- **Vulnerability scoring system** — Quantified security ratings per server  
- **Interactive dashboard** — Professional visualization of results  
- **Report generation** — PDF, JSON, and CSV export capabilities  

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot, HAPI FHIR Client |
| Database | PostgreSQL |
| Frontend | Angular |
| Persistence | JPA / Hibernate |

---

## 12-Week Development Plan

### Phase 1 — Foundation & Core Infrastructure (Weeks 1–3)

#### Week 1 — FHIR Research & Core Connectivity

**Objective:** Understand FHIR fundamentals and establish backend communication with public FHIR servers.

**Tasks:**
- Study FHIR REST architecture (Patient, Observation, Encounter, etc.)
- Set up Spring Boot backend
- Integrate HAPI FHIR client
- Connect to at least one public FHIR test server
- Implement: `GET /Patient`, `GET /Observation`, `POST Patient` (if allowed)

**Deliverable:** Working backend capable of interacting with a FHIR server; demonstration of successful read/write operations; high-level architecture diagram.

> *"The system successfully communicates with public FHIR servers."*

---

#### Week 2 — Database & Server Management Layer

**Objective:** Persist server configurations and test results.

**Tasks:**
- Set up PostgreSQL
- Implement JPA entities: `FhirServer`, `AttackScenario`, `TestRun`, `TestResult`
- Create REST endpoints: Add server, Remove server, List servers
- Basic Angular UI for server management

**Deliverable:** Servers can be added via UI; backend stores configurations; basic frontend connected to backend.

> *"The system now manages multiple FHIR servers and persists configurations."*

---

#### Week 3 — Attack Framework Architecture

**Objective:** Design the extensible attack engine.

**Tasks:**
- Create `AttackScenario` interface
- Implement `AttackExecutorService`
- Design modular attack structure
- Implement first simple attack: Malformed JSON request

**Deliverable:** First automated attack can be executed; HTTP response is stored and logged; result visible in UI.

> *"The platform executes automated attack scenarios."*

---

### Phase 2 — Attack Engine Implementation (Weeks 4–7)

#### Week 4 — Malformed Request Testing

**Objective:** Test server validation robustness.

**Implement attacks:**
- Invalid JSON structure
- Duplicate fields
- Unexpected fields
- Broken metadata elements
- Incorrect resource types

**Deliverable:** Comparative results across servers; identification of weak validation behavior.

> *"Some servers improperly validate malformed requests."*

---

#### Week 5 — Hidden Data Injection & Covert Channels

**Objective:** Test whether hidden data can be embedded undetected.

**Implement attacks:**
- Misuse of extension fields
- Manipulated identifiers
- Embedded contained resources
- Additional unexpected JSON payload fragments
- Encoded hidden data

**Deliverable:** Detection of covert channel opportunities; report on fields allowing hidden payload insertion.

> *"Certain fields allow hidden data insertion without detection."*

---

#### Week 6 — Resource Manipulation & Access Testing

**Objective:** Detect improper access controls.

**Implement:**
- Cross-patient access attempts
- Owner/reference manipulation
- ID tampering
- Unauthorized resource retrieval attempts

**Deliverable:** Report on access control enforcement; identification of privilege escalation risks.

> *"Some servers allow improper resource access."*

---

#### Week 7 — Authentication & Authorization Testing

**Objective:** Evaluate authentication mechanisms.

**Implement:**
- Detection of open endpoints
- Basic Auth testing
- OAuth2 flow simulation
- Token misuse attempts
- Expired/invalid token tests

**Deliverable:** Classification of authentication strategies; identification of weak or missing auth enforcement.

> *"Authentication enforcement varies significantly across servers."*

---

### Phase 3 — Security Analysis & Intelligence (Weeks 8–10)

#### Week 8 — Data Leakage & Error Analysis

**Objective:** Identify excessive data exposure.

**Implement:**
- Detection of overly verbose error messages
- Internal ID leakage
- Unexpected resource exposure
- Metadata leakage analysis

**Deliverable:** Leakage classification report; evidence-based vulnerability records.

> *"The system detects unintended data exposure patterns."*

---

#### Week 9 — Vulnerability Scoring System

**Objective:** Create a structured evaluation model.

**Implement:**
- Severity classification (Low / Medium / High / Critical)
- Risk scoring algorithm
- Server security score calculation
- Aggregated vulnerability report

**Deliverable:** Each server receives a quantified security rating; comparative ranking between servers.

> *"The system now provides a structured security rating."*

---

#### Week 10 — Assignment closure: leakage evidence, vectors, UI interpretation

**Aligned with project brief — *FHIR Security Testing App: Which attacks are my servers vulnerable to?***

**Objective:** Tie stored results to the specification: **data leakage paths**, **which fields/vectors** allowed suspicious acceptance, and **interpretation** in the UI—without scope creep beyond the brief.

**Tasks:**
- **Data leakage** — Ensure probes (or dedicated scenarios) capture when error/edge responses expose stack traces, verbose internals, or unexpected identifiers; persist a clear **leakage / exposure signal** alongside existing run results.
- **Blind spots & vectors** — Persist **analysis metadata** that names the tested vector (e.g. extension URL, `contained`, duplicate JSON keys, encoded display) so results can be **aggregated** by vector, not only by free-text reason.
- **Frontend** — Improve **aggregation and interpretation**: filters or grouping by classification/category, clearer per-scenario evidence (status, classification, reason), building on the existing runner and server-compare views.

**Deliverable:** For a test run, a reviewer can see **what leaked (if anything), what was accepted, and which named attack vector** produced each row.

> *"Understanding these blind spots enables identifying specific fields where unexpected content could remain unnoticed."*

---

### Phase 4 — Reporting, Hardening & Finalization (Weeks 11–12)

#### Week 11 — Assignment closure: auth context, Observation-style misuse, optional real-token checks

**Aligned with project brief** (authentication strategies; access restricted to the authenticated user vs cross-patient / escalation; example of **Observation** misuse).

**Objective:** Close the remaining **authorization narrative** and the brief’s **clinical-content misuse** example, within ethical limits on public test servers.

**Tasks:**
- **Authenticated isolation (where possible)** — If a **lab** server and **test client credentials** are available via environment/config (no secrets in git): with a **valid** token, probe read/write **outside** the granted patient/context; otherwise record the scenario as **N/A** with a short justification (still honest relative to *“when authentication is present”*).
- **Observation-oriented misuse** — Add or extend at least one scenario aligned with the brief (e.g. **Observation** chained to an existing `Patient`, or a **Bundle / transaction** path that tests whether **extra** clinical entries slip through policy), distinct from pure anonymous IDOR where you already have coverage.
- **Backend unit tests (important only)** — Add focused unit tests for the Week 11 critical paths (new/updated attacks and classification rules), aiming to prevent false positives; not exhaustive coverage.

**Deliverable:** Per-server auth/isolation outcomes visible via **attack scenarios** (Open Endpoint, Cross-Patient, Token Isolation, etc.) in the runner and compare views.

> *"When authentication is present, the system evaluates whether access is correctly restricted… or whether privilege escalation or cross-patient access is possible."*

---

#### Week 12 — Refinement & Finalization

**Objective:** Freeze scope to the **stated full-stack deliverable**: application layer, backend/database (configs, results, **payloads / analysis metadata**), frontend (**visual inspection, aggregation, interpretation**).

**Tasks:**
- Verify persistence and API expose enough to **reproduce each finding** (request fingerprint or stable scenario id, response handling policy, classification, severity).
- Align naming across backend scenarios, API, and Angular labels; refresh architecture notes if needed.
- Demo on **2–3 public servers** (recorded run + screenshots or script): **inspect → aggregate → interpret**.
- Short **limitations** section (public sandboxes, token availability, no real PHI).

**Final Deliverable:** A full-stack FHIR Security Testing Platform featuring:

| Feature | Status |
|---------|--------|
| Automated attack engine | ✓ |
| Authentication analysis | ✓ |
| Covert channel detection | ✓ |
| Data leakage detection | ✓ |
| Vulnerability scoring system | ✓ |
| Interactive dashboard | ✓ |
| Report generation | ✓ |

---

## Project Goals

This project is designed to be:

- **A security research platform** — Systematic evaluation of FHIR server security
- **A practical vulnerability assessment tool** — Usable for real-world assessments
- **A strong portfolio project** — Demonstrating backend and security engineering skills

---

## Getting Started

### Prerequisites

- **Java 21**
- **Maven 3.6+** (or use the included Maven wrapper)

### Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows:

```bash
cd backend
mvnw.cmd spring-boot:run
```

The API runs at **http://localhost:8080**. See [backend/README.md](backend/README.md) for API endpoints and configuration.

---

## License

*To be determined.*
