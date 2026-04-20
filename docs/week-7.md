# Week 7 — Authentication & Authorization Testing (Daily Planning)

## How this connects to the existing codebase (read first)

| Piece | Role in Week 7 |
|--------|----------------|
| **`ExecutableAttack` + `AttackResult`** | Each auth scenario stays a single pluggable attack; register new classes as `@Component` beans. |
| **`AttackRegistry` + `AttackExecutorService`** | No new orchestration endpoints; runs are still `POST /api/attacks/run/{serverId}` and results `GET /api/results/{testRunId}`. |
| **`AttackHttpClient`** | Already has `post` / `get` / `put`. **Week 7 requires sending custom headers** (e.g. `Authorization: Basic …`, `Authorization: Bearer …`). Plan to extend with overloads such as `get(url, HttpHeaders)` / `post(url, body, HttpHeaders)` or a small `execute(url, method, body, headers)` helper. |
| **`FhirServer`** | Has `authenticationType` (string) for classification in UI/report; **it does not store secrets today**. For Basic Auth / Bearer tests you will either: (a) add optional encrypted fields (`basicUser`, `basicPassword`, `bearerToken`, etc.), (b) read test credentials from `application.properties` / env for known lab servers only, or (c) run token-misuse tests with **syntactically valid but wrong** tokens without server-specific secrets. Document the chosen approach in the report. |
| **`attack-runner.ts`** | Same pattern as Week 6: add a `authScenarioNames` array (like `accessControlScenarioNames`) and a third summary line: *“X of Y auth-related attacks …”* |

---

## Weekly Goal

By Friday, you should have:

- Implemented **five** authentication-focused test flows (aligned with the week brief):
    - **Open endpoint detection** — infer whether sensitive operations are reachable without credentials (and optionally compare to `/metadata` / `CapabilityStatement`).
    - **Basic Auth probing** — requests with missing, wrong, or malformed Basic credentials (behavior vs expected 401/403).
    - **OAuth2 / SMART-style flow simulation** — a **controlled** subset (e.g. probe `well-known` / `authorize` / `token` URLs, optional client-credentials if your test server supports it); full browser redirect flows are usually out of scope unless you integrate a headless step.
    - **Token misuse** — valid-shaped Bearer token on wrong resource, or Bearer + GET when only POST should be allowed, etc. (pick 1–2 clear rules per server).
    - **Expired / invalid token** — send malformed JWT, expired-looking token, or empty Bearer and record status codes.
- A **visible result summary** in the UI for Week 7 scenario names (count vulnerable vs total for those attacks only).
- Verification on **2–3 FHIR servers** with recorded `statusCode` / `vulnerable` in results (and notes in the report).
- A written report **`docs/week-7.md`** (separate “results” section or extend this file after the week) covering:
    - **Classification of authentication strategies** (from metadata + observed behavior).
    - **Weak or missing auth enforcement** (where anonymous or wrong-auth requests succeed unexpectedly).

**Demonstration line:**

> “Authentication enforcement varies significantly across servers.”

---

## Vulnerability rules (Week 7 — scenario-specific)

Week 6 used **method-aware** rules for access control. Auth tests are different: a **401/403** often means *good*. Define rules **per attack** and keep them consistent in code and report.

Suggested baseline (adjust after first runs against real servers):

| Category | Idea | Example “vulnerable” signal |
|----------|------|-----------------------------|
| Open endpoints | Clinical write/read without `Authorization` when server advertises SMART/OAuth | e.g. `POST /Patient` returns **200/201** with no auth while metadata lists OAuth URIs |
| Basic Auth | Wrong password | **200/201** on protected resource (or **500** instead of auth error) → fragile |
| OAuth2 simulation | Invalid client / wrong grant | Unexpected **200** with sensitive body from token endpoint; or metadata promises OAuth but no `authorize`/`token` URLs |
| Token misuse | Bearer on wrong scope/resource | **200/201** with victim data when token should not apply |
| Invalid/expired token | Garbage or expired Bearer | **200/201** or **500** instead of **401/403** |

You may align numeric flags with the project convention where it still makes sense:

- `vulnerable = (statusCode == 200 || statusCode == 201 || statusCode == 500)` **only where “success / server error instead of auth denial” indicates a problem** for that step.

Document any deviation (e.g. “401 = not vulnerable” explicitly).

---

## Day 1 — Threat model + HTTP/auth plumbing

**Goal:** Know exactly what each Week 7 attack will send and extend the client so attacks can attach headers.

1. **Hours 1–2 — Map surfaces**
    - Which URLs are in scope: `/metadata`, `/.well-known/smart-configuration`, `/Patient`, token endpoint (if discoverable).
    - What “open” means for your thesis: e.g. anonymous `GET /Patient/{id}` vs `POST` without auth.

2. **Hours 3–4 — `AttackHttpClient` design**
    - Add header-capable methods or shared `execute` with `HttpHeaders`.
    - Ensure **timeouts** and error handling stay consistent with existing `HttpResult` (statusCode + body).

3. **Hours 5–6 — Credential strategy**
    - Decide: DB fields vs env-only test secrets vs only “fake” tokens.
    - If storing secrets: never commit real passwords; use env vars in deployment notes.

4. **Hours 7–8 — Name the five classes (indicative)**

| Planned class (indicative) | Focus |
|----------------------------|--------|
| `OpenEndpointDetectionAttack.java` | Unauthenticated probes + optional metadata comparison |
| `BasicAuthTestingAttack.java` | Wrong/missing Basic `Authorization` |
| `OAuth2FlowSimulationAttack.java` | well-known + authorize/token probes (subset) |
| `TokenMisuseAttack.java` | Bearer misuse / wrong context |
| `InvalidOrExpiredTokenAttack.java` | malformed / expired-style Bearer |

**End of Day 1 deliverable**

- ✔ Header support in `AttackHttpClient` (or equivalent).
- ✔ Credential strategy documented (even if “no real secrets in repo”).
- ✔ Per-attack vulnerability rules sketched in a table.

---

## Day 2 — Open endpoints + Basic Auth

**Goal:** First two attacks executable end-to-end.

1. **Hours 1–3 — `OpenEndpointDetectionAttack`**
    - Issue unauthenticated `GET`/`POST` (as appropriate to your thesis) to endpoints that should require auth on SMART servers.
    - Optionally fetch `/metadata` or SMART configuration JSON and compare advertised `oauth` vs observed anonymous access.

2. **Hours 4–6 — `BasicAuthTestingAttack`**
    - Send `Authorization: Basic <invalid>` or missing header when Basic is expected (if you can detect from metadata or server docs).
    - Record status codes; label vulnerable per your Day 1 table.

3. **Hours 7–8 — Run on 1 server**
    - Full run; confirm rows in UI with `statusCode` and `vulnerable`.

**End of Day 2 deliverable**

- ✔ Two attacks implemented + verified on at least one server.

---

## Day 3 — OAuth2 flow simulation

**Goal:** A defensible “simulation” that fits a bachelor timeline (not necessarily a full browser OAuth dance).

1. **Hours 1–2 — Discovery**
    - Parse `/.well-known/smart-configuration` or `CapabilityStatement.rest.security` when available.
    - Extract `authorization_endpoint`, `token_endpoint` (if present).

2. **Hours 3–5 — `OAuth2FlowSimulationAttack`**
    - Implement **safe** probes: e.g. `GET` well-known, `POST` token endpoint with invalid `client_id` / `grant_type` and expect 400/401 — flag if server returns 200 with tokens incorrectly.
    - Optionally document “simulation” as **metadata + invalid grant probe**, not full authorization-code user login.

3. **Hours 6–8 — Test on 1–2 servers**
    - Note servers without SMART URLs — report as “not applicable” instead of false vulnerable.

**End of Day 3 deliverable**

- ✔ OAuth-related attack implemented + tested where URLs exist.

---

## Day 4 — Token misuse + invalid/expired token

**Goal:** Complete the remaining two Bearer-focused attacks.

### Part A — Token misuse

1. **Hours 1–3 — `TokenMisuseAttack`**
    - Examples: Bearer token for resource A used against resource B; or valid-format random JWT against `/Patient`.
    - Define one clear misuse rule per run so results are explainable.

### Part B — Invalid / expired token

1. **Hours 4–6 — `InvalidOrExpiredTokenAttack`**
    - Send **`Bearer` + malformed string**, **empty Bearer**, or fixed “expired” sample JWT (many servers return 401 regardless — that’s OK; you document).

2. **Hours 7–8 — Test on 1–2 servers**
    - Compare behavior: some servers leak stack traces on bad JWT (**500**) — may count as vulnerable per your table.

**End of Day 4 deliverable**

- ✔ Token misuse + invalid/expired token attacks implemented + tested.

---

## Day 5 — UI summary + Week 7 report + demo prep

**Goal:** Same polish as Week 6 — easy to demo and defend in writing.

1. **Hours 1–2 — Angular summary**
    - Add `authScenarioNames` matching **exact** `getName()` strings from Java.
    - Display: **`X of Y auth-related attacks vulnerable`** (wording can match your thesis language).

2. **Hours 3–5 — Report (`docs/week-7-results.md` or a “Results” section)**
    - Table: `Server | Open endpoints | Basic Auth | OAuth2 simulation | Token misuse | Invalid token`
    - Short conclusion per server: **classification** (e.g. anonymous read allowed, OAuth advertised but not enforced).
    - **Weak/missing enforcement:** bullet list with references to scenario names.

3. **Hours 6–7 — Runs on 2–3 servers**
    - Export or screenshot key rows; store `testRunId` references like in Week 6.

4. **Hour 8 — Demo script**
    - One slide or paragraph per category; end with:

> “Authentication enforcement varies significantly across servers.”

**End of Day 5 deliverable**

- ✔ UI summary for Week 7 auth scenarios.
- ✔ Written classification + weak/missing enforcement documented.
- ✔ Demo narrative prepared.

---

## Registration & UI checklist (same pattern as Week 6)

- [ ] All five attacks are Spring `@Component` beans (auto-discovered by `AttackRegistry`).
- [ ] `getName()` strings are **stable** and duplicated exactly in `authScenarioNames` in `attack-runner.ts`.
- [ ] New attacks appear in the results table like existing rows (Attack | Status Code | Vulnerable).
- [ ] `AttackScenario` rows in DB populate on first run via existing `AttackExecutorService` flow.

---

## Risk notes (thesis honesty)

- **Public test servers** often allow anonymous read by design — your report should distinguish *misconfiguration* vs *intentional public sandbox*.
- **OAuth2 “simulation”** should be described accurately (metadata + token endpoint probes), not overstated as full OAuth user login unless implemented.
- **Secrets:** if you add Basic credentials to `FhirServer`, describe storage risk and mitigation (env, no git).
