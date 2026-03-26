# Week 5 — Summary

## Hidden Data Injection & Covert Channels

**Weekly Goal:** Test whether hidden data can be embedded in FHIR requests undetected—extensions, identifiers, contained resources, extra JSON, and encoded strings—and surface results in the UI with a dedicated covert-channel summary.

---

## What Was Achieved

### ✅ Five Covert Channel Attacks Implemented

All attacks POST to `/Patient` using raw JSON via `AttackHttpClient`.

| Attack | Purpose | Payload (abbrev.) |
|--------|---------|-------------------|
| **Extension Fields Misuse** | Custom extension URL + `valueString` | `extension` with `http://malicious.example/hidden` / `covert-payload` |
| **Manipulated Identifiers** | Null byte in `id`, delimiter-like `identifier` | `id`: `visible` + `\u0000` + `hidden-data`; `value`: `id;secret=1` |
| **Embedded Contained Resources** | Covert data in `contained` Binary | `contained` → `Binary` with base64 `c2VjcmV0LWRhdGE=` |
| **Unexpected JSON Fragments** | Extra top-level / nested keys | `_payload`, `extraNested.secret` |
| **Encoded Hidden Data** | Unicode escapes in `meta.tag` display | `\u0053\u0065\u0063\u0072\u0065\u0074` (“Secret”) |

### ✅ Attack Implementation Pattern

- Each attack implements `ExecutableAttack`
- Uses `AttackHttpClient.post()` for raw HTTP requests
- `@Component` for automatic registration in `AttackRegistry`
- Shared vulnerability rule: acceptance or server error indicates weak handling

### ✅ Package Structure (attack/ additions)

```
attack/
├── ExtensionFieldsMisuseAttack.java
├── ManipulatedIdentifiersAttack.java
├── EmbeddedContainedResourcesAttack.java
├── UnexpectedJsonFragmentsAttack.java
└── EncodedHiddenDataAttack.java
```

*(Week 4 malformed attacks and framework types unchanged: `ExecutableAttack`, `AttackResult`, `AttackRegistry`, etc.)*

### ✅ Vulnerability Evaluation

| Status Code | Meaning |
|-------------|---------|
| 4xx (other than above “bad”) | Server rejects or preconditions fail — **not vulnerable** for this rule set |
| **200** | OK — server accepted request (possibly stored covert payload) — **vulnerable** |
| **201** | Created — resource persisted — **vulnerable** |
| **500** | Server error / crash on payload — **vulnerable** |

Rejection with **400** (or e.g. **412**) is treated as correct handling → **not vulnerable**.

### ✅ Angular UI Updates

- **Overall summary:** `X of Y attacks vulnerable` (all scenarios in the run)
- **Covert summary:** `X of Z covert channel attacks allow hidden data`
- `covertChannelNames` matches the five attacks above
- `getCovertChannelResultCount()` — how many covert rows appear in the run
- `getCovertChannelVulnerableCount()` — how many of those are flagged vulnerable
- Results table unchanged: Attack | Status Code | Vulnerable (✓ OK / ⚠ Vulnerable)

---

## Technical Highlights

### No New Attack Orchestration Endpoints

- Same flow as prior weeks: `POST /api/attacks/run/{serverId}`, `GET /api/results/{testRunId}`
- New `@Component` beans are picked up automatically by `AttackRegistry`

### Testing Workflow

1. Run attacks via API for a configured `serverId`
2. Load results by `testRunId` and confirm status codes and `vulnerable` flags
3. Open **Security Test Runner** — all scenarios (malformed + covert) appear in one table; covert line shows when any of the five covert results are present

---

## Deliverables Checklist

| Deliverable | Status |
|-------------|--------|
| ExtensionFieldsMisuseAttack | ✔ |
| ManipulatedIdentifiersAttack | ✔ |
| EmbeddedContainedResourcesAttack | ✔ |
| UnexpectedJsonFragmentsAttack | ✔ |
| EncodedHiddenDataAttack | ✔ |
| All covert attacks registered and executed with malformed suite | ✔ |
| Covert channel summary in UI | ✔ |
| API + UI verification of new rows and counts | ✔ |

---

## Quote

> *"Certain fields allow hidden data insertion without detection."*

---

## Next Steps (Week 6)

Per project plan: resource manipulation & access testing—cross-patient access, reference manipulation, ID tampering, unauthorized retrieval—and reporting on access-control enforcement.
