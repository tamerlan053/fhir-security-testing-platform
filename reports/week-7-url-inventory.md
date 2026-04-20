# Week 7 — URLs in scope (Day 1 inventory)

Manual probes with **no authentication** (Postman / browser). Record **HTTP status** and short notes for each `{base}` under test.

---

## Template (copy per server)

| Full URL | Purpose | HTTP status | Notes |
|----------|---------|-------------|-------|
| `{base}/metadata` | CapabilityStatement, security metadata | | |
| `{base}/.well-known/smart-configuration` | SMART discovery (`authorization_endpoint`, `token_endpoint`) | | |
| `{base}/Patient` | Anonymous access to Patient search (GET) | | |

If SMART returns **200**, copy `authorization_endpoint` and `token_endpoint` from the JSON body into the notes column.

---

## SMART: `authorization_endpoint` & `token_endpoint`

For the three public servers documented below, **`GET {base}/.well-known/smart-configuration` did not return HTTP 200**, so the SMART JSON (with `authorization_endpoint`, `token_endpoint`, `scopes_supported`, …) **could not be retrieved** from the usual discovery URL.

| Item | Status |
|------|--------|
| `authorization_endpoint` | **Not available** from well-known on these bases (404 / 501 — see per-server tables). |
| `token_endpoint` | **Not available** from well-known on these bases. |

**Day 1 note:** *Token/authorize when discoverable* is **N/A** for this inventory until a server exposes a working `/.well-known/smart-configuration` (200) or OAuth URIs are taken from CapabilityStatement `rest.security` (not extracted in this manual pass). Future Week 7 code can parse metadata for `oauth2` extensions if present.

---

## http://hapi.fhir.org/baseR4

| Full URL | Purpose | HTTP status | Notes |
|----------|---------|-------------|-------|
| `http://hapi.fhir.org/baseR4/metadata` | CapabilityStatement | **200 OK** | JSON `CapabilityStatement`; `software.name` HAPI FHIR Server, `fhirVersion` 4.0.1; endpoint reachable without auth. |
| `http://hapi.fhir.org/baseR4/.well-known/smart-configuration` | SMART discovery | **404 Not Found** | `OperationOutcome` — server treats path as FHIR resource type; no SMART well-known at this path on this instance. |
| `http://hapi.fhir.org/baseR4/Patient` | Anonymous Patient search | **200 OK** | `Bundle` type `searchset` — public read/search on this demo server (expected for HAPI test). |

### Summary (HAPI public)

- Metadata and unauthenticated `GET Patient` succeed: **public test/demo behaviour**, not a production security posture.
- SMART on FHIR discovery URL **not available** at the standard path (404).

---

## https://server.fire.ly/r4

| Full URL | Purpose | HTTP status | Notes |
|----------|---------|-------------|-------|
| `https://server.fire.ly/r4/metadata` | CapabilityStatement | **200 OK** | Large JSON (~1.9 MB); `CapabilityStatement` for Firely Server **6.7.x**, publisher Firely, `experimental: true` — reachable without auth. |
| `https://server.fire.ly/r4/.well-known/smart-configuration` | SMART discovery | **501 Not Implemented** | `OperationOutcome`: issue code `not-supported`, diagnostics `"/.well-known"` — SMART well-known path not supported on this endpoint. |
| `https://server.fire.ly/r4/Patient` | Anonymous Patient search | **200 OK** | `Bundle` type `searchset` with `Patient` entries (e.g. test patients) — unauthenticated search returns data. |

### Summary (Firely public)

- `/metadata` and unauthenticated `GET Patient` return **200** (public demo server behaviour).
- `/.well-known/smart-configuration` returns **501** — no SMART discovery at this path.

## https://r4.smarthealthit.org

| Full URL | Purpose | HTTP status | Notes |
|----------|---------|-------------|-------|
| `https://r4.smarthealthit.org/metadata` | CapabilityStatement | **200 OK** | `CapabilityStatement`; `software.name` Smile CDR, `fhirVersion` 4.0.0, `rest.security` includes `cors: true`; public access. |
| `https://r4.smarthealthit.org/.well-known/smart-configuration` | SMART discovery | **404 Not Found** | `OperationOutcome` — `Unknown resource type '.well-known'` (same pattern as HAPI: path not exposed as SMART well-known). |
| `https://r4.smarthealthit.org/Patient` | Anonymous Patient search | **200 OK** | `Bundle` type `searchset`, pagination links — unauthenticated Patient search returns entries. |

### Summary (SMART Health IT R4)

- `/metadata` and `GET Patient` return **200** without auth (public test server profile).
- `/.well-known/smart-configuration` returns **404** — no SMART discovery document at this URL.

## Azure API for FHIR — skipped in this inventory

| | |
|--|--|
| **URL tested** | `https://fhir.azurehealthcareapis.com` (bare host only) |
| **Status** | **Not probed** for Day 1 |

**Reason:** Azure Health Data Services FHIR typically requires a **workspace-specific** base URL (and often **authenticated** access). Using the marketing/root host without a provisioned workspace is not comparable to the other three public sandboxes. Documented here so the four-server list is explicit: **three servers fully inventoried; Azure deferred** until a valid base URL exists in the Azure portal.

To complete a fourth row later: use the FHIR base URL from **Azure Portal → your FHIR service**, then run the same three GETs (`/metadata`, `/.well-known/smart-configuration`, `/Patient`) with or without auth as required by that instance.

---

## Related Day 1 reports

- `reports/week-7-credential-policy.md` — how secrets are handled for Week 7 tests.
- `reports/week-7-scenario-vulnerable.md` — draft “scenario → vulnerable” table for the five planned attacks.
