# Week 7 — Scenario → vulnerable condition (Day 1)

Draft rules for the five planned authentication-related attacks. **Refine** after implementation and real server runs (Days 2–4). Java class names are indicative; `getName()` strings for the UI must match exactly when implemented.

| Planned Java class | Display name (indicative) | Vulnerable when (draft) |
|--------------------|---------------------------|-------------------------|
| `OpenEndpointDetectionAttack` | Open Endpoint Detection | Unauthenticated access succeeds where the server’s **metadata** or policy implies OAuth/SMART protection (e.g. `POST` or sensitive `GET` returns **200/201** without auth when SMART URIs exist in CapabilityStatement — * refine per server *) **or** anonymous write/search where policy states auth required. *For pure public sandboxes, interpret as informational — document in thesis.* |
| `BasicAuthTestingAttack` | Basic Auth Testing | Request with **wrong** `Authorization: Basic` (or missing when Basic is required) still yields **200/201** on a protected resource, or **500** instead of a clear **401/403**. |
| `OAuth2FlowSimulationAttack` | OAuth2 Flow Simulation | **Token endpoint** accepts invalid `client_id` / grant and returns **200** with tokens, or **metadata promises OAuth** but no reachable authorize/token URLs and behaviour contradicts advertised security. *(If SMART well-known is unavailable, attacks may probe alternate documented endpoints only — see URL inventory.)* |
| `TokenMisuseAttack` | Token Misuse | Bearer token **accepted** for a resource or operation it should not cover (e.g. wrong scope/context) — **200/201** with sensitive data when **403/401** expected. |
| `InvalidOrExpiredTokenAttack` | Invalid or Expired Token | Malformed / clearly invalid Bearer returns **200/201**, or **500** with stack trace instead of **401/403**. |

## Alignment with existing project convention

Where “success or opaque server error” indicates bad validation, some steps may reuse:

`vulnerable = (statusCode == 200 || statusCode == 201 || statusCode == 500)`

**Only** when that semantic matches the row above; auth rejection (**401/403**) is usually **not** vulnerable.

---

## Implementation note

Concrete `ExecutableAttack` classes are **scheduled after Day 1** (see `docs/week-7.md`). Day 1 delivers this table plus header-capable `AttackHttpClient`.
