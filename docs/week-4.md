# Week 4 — Summary

## Malformed Request Testing

**Weekly Goal:** Implement malformed request attacks to test server validation robustness, identify weak validation behavior, and display comparative results in the UI.

---

## What Was Achieved

### ✅ Six Malformed Request Attacks Implemented

| Attack | Purpose | Payload |
|--------|---------|---------|
| **Malformed JSON Request** | Truncated JSON | `{ "resourceType": "Patient", ` |
| **Invalid JSON Structure** | Trailing comma (RFC 8259) | `{ "resourceType": "Patient", }` |
| **Duplicate Fields** | Duplicate keys | `{"id":"valid-id","id":"duplicate-id"}` |
| **Unexpected Fields** | Non-FHIR fields | `unknownField`, `__proto__` |
| **Broken Metadata** | Invalid meta types | `meta.versionId: 123` (number) |
| **Incorrect Resource Type** | Fake resourceType | `resourceType: "FakeResource"` |

All attacks POST to `/Patient` and evaluate: 400 = correct validation, 200 or 500 = vulnerable.

### ✅ Attack Implementation Pattern

- Each attack implements `ExecutableAttack`
- Uses `AttackHttpClient.post()` for raw HTTP requests
- `@Component` for automatic registration in `AttackRegistry`
- Same vulnerability rule across all attacks

### ✅ Package Structure ( attack/ )

```
attack/
├── ExecutableAttack.java (interface)
├── AttackResult.java (record)
├── AttackRegistry.java
├── MalformedJsonAttack.java
├── InvalidJsonStructureAttack.java
├── DuplicateFieldsAttack.java
├── UnexpectedFieldsAttack.java
├── BrokenMetadataAttack.java
└── IncorrectResourceTypeAttack.java
```

### ✅ Angular UI Updates

- **Summary:** "X of Y attacks vulnerable" displayed above results table
- `getVulnerableCount()` method in `AttackRunnerComponent`
- All 6 attacks displayed with status code and vulnerable flag
- Results table shows Attack | Status Code | Vulnerable (✓ OK / ⚠ Vulnerable)

### ✅ Vulnerability Evaluation

| Status Code | Meaning |
|-------------|---------|
| 400 | Correct validation (server rejects) |
| 200 | Bad validation (accepts malformed data) |
| 500 | Server crash |

---

## Technical Highlights

### No Backend API Changes

- Existing endpoints reused: `POST /api/attacks/run/{serverId}`, `GET /api/results/{testRunId}`
- `AttackRegistry` automatically collects new `@Component` attacks

### Frontend

- Summary line: `{{ getVulnerableCount() }} of {{ currentRun.results.length }} attacks vulnerable`

---

## Deliverables Checklist

| Deliverable | Status |
|-------------|--------|
| Invalid JSON structure attack | ✔ |
| Duplicate fields attack | ✔ |
| Unexpected fields attack | ✔ |
| Broken metadata attack | ✔ |
| Incorrect resource types attack | ✔ |
| All attacks registered and executed | ✔ |
| Results table shows all attacks | ✔ |
| Summary (X of Y vulnerable) | ✔ |
| Weak validation visible in UI | ✔ |

---

## Quote

> *"Some servers improperly validate malformed requests."*

---

## Next Steps (Week 5)

- Add side-by-side server comparison component
- Implement `GET /api/results/compare?serverIds=1,2,3`
- Document validation behavior per server in report
- Add severity or category to results
- Implement additional attack types (injection, access control)
