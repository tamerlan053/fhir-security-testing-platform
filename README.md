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

**Deliverable:** Each server receives a quantified security score; comparative ranking between servers.

> *"The system now provides a structured security rating."*

---

#### Week 10 — Advanced Frontend Dashboard

**Objective:** Professional visualization of results.

**Implement in Angular:**
- Security dashboard
- Charts (vulnerability distribution)
- Server comparison view
- Filtering by severity/type
- Attack execution history

**Deliverable:** Interactive security dashboard; clear visualization of vulnerabilities.

> *"A complete security analysis dashboard."*

---

### Phase 4 — Reporting, Hardening & Finalization (Weeks 11–12)

#### Week 11 — Report Generation & Export

**Objective:** Generate professional reports.

**Implement:**
- PDF report generation
- JSON export
- CSV export
- Executive summary per server
- Technical vulnerability breakdown

**Deliverable:** Downloadable security assessment report; academic-quality output.

> *"The system generates professional security reports."*

---

#### Week 12 — Refinement & Finalization

**Objective:** Polish and finalize the project.

**Tasks:**
- Code refactoring
- Performance improvements
- Add 1–2 advanced attack scenarios
- Write technical documentation
- Prepare final presentation
- Prepare demonstration dataset

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
