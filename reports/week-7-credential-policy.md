# Week 7 — Credential policy (Day 1)

This document fixes how authentication secrets are handled for the FHIR security testing platform and the bachelor thesis work. It satisfies the Day 1 requirement: *credential policy (env vs DB vs token-only probes) — write it down for the report*.

---

## Decision

| Approach | Status |
|----------|--------|
| Store real passwords or long-lived **Bearer** tokens **in the Git repository** | **Not used** |
| Optional future **database fields** on `FhirServer` for Basic credentials or tokens | **Out of scope for Week 7 Day 1**; if added later, document encryption / vault use |
| **Synthetic probes only** for Week 7 attacks (wrong password, malformed JWT, random Bearer string) | **Primary approach** — no real user credentials required |
| **Environment variables** or local `application.yml` **excluded from version control** (`.gitignore`) for lab-only test credentials | **Allowed** if a specific FHIR server requires a real token for a one-off manual comparison |

---

## Rationale

- Thesis repositories and coursework repos must not contain production or personal health-related credentials.
- Public test servers (HAPI, Firely, SMART Health IT) in this project are probed **without** stored secrets for baseline reconnaissance.
- Attacks such as Basic Auth misuse and invalid Bearer tests are meaningful using **intentionally invalid** values (e.g. `Authorization: Basic` with wrong base64, `Bearer` + garbage string) to observe HTTP **401/403** vs incorrect **200/201/500**.

---

## Review

Revisit this policy before adding UI fields for “server password” or OAuth client secrets.
