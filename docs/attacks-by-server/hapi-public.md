# Security test run documentation: HAPI Public

| Field | Value |
|------|----------|
| **Server** | HAPI Public |
| **Base URL** | `http://hapi.fhir.org/baseR4` |
| **Database test run ID** | `test_run_id = 5` |
| **Run startedAt** | `2026-04-22T10:32:47.503153` |
| **Response bodies below** | As stored in PostgreSQL, column `test_result.response_body` (run 5) |

## Classification legend

| Classification | Meaning |
|---------------|--------|
| **SECURE** | Expected rejection or correct behavior for a "plain" request. |
| **VULNERABLE** | Confirmed risk (counts toward vulnerability totals). |
| **OPEN_POLICY** | Behavior matches explicit public/demo policy (OAuth not advertised in metadata). |
| **MISCONFIGURED** | Advertised security (OAuth/SMART) is inconsistent with actual anonymous access. |
| **INCONCLUSIVE** | No conclusion possible (error, incomplete setup, non-standard response). |

**Note:** For OAuth-aware scenarios, the classifier compares request success with whether OAuth/SMART is advertised in the environment (see `AttackOutcome` in code).

---

## Summary table: all scenarios (run 5)

| # | Scenario | HTTP | Classification | Vulnerable (legacy) | Severity |
|---|----------|------|---------------|---------------------|-------------|
| 1 | Malformed JSON Request | 400 | SECURE | no | INFO |
| 2 | Metadata Manipulation | 412 | SECURE | no | INFO |
| 3 | Unexpected Payload Injection | 412 | SECURE | no | INFO |
| 4 | Extension Fields Misuse | 201 | VULNERABLE | yes | MEDIUM |
| 5 | Contained Resource Smuggling | 201 | VULNERABLE | yes | MEDIUM |
| 6 | Encoded Hidden Data | 412 | SECURE | no | INFO |
| 7 | Invalid Credentials Access Test | 200 | VULNERABLE | yes | HIGH |
| 8 | Open Endpoint Detection | 200 | MISCONFIGURED | no | MEDIUM |
| 9 | Cross-Patient Access | 200 | VULNERABLE | yes | HIGH |
| 10 | Unauthorized Write / ID Tampering | 200 | VULNERABLE | yes | CRITICAL |

---

## Per-scenario detail

For **each** scenario below: what the test does (backend implementation), what was observed on the server, and a short interpretation.

### 1. Malformed JSON Request

**Procedure:** two POSTs to `http://hapi.fhir.org/baseR4/Patient`: (a) truncated JSON `{ "resourceType": "Patient", `; (b) JSON with a trailing comma `{ "resourceType": "Patient", }`. Behavioral classification: **VULNERABLE** only if malformed JSON leads to a **retrievable created Patient** (POST→GET verification); `400/412/422/...` rejection → **SECURE**.

**Result (5):** HTTP **400**, **SECURE**.

**Reason:** Truncated JSON: server rejected malformed JSON as expected. | Also: SECURE — Trailing comma JSON: server rejected malformed JSON as expected.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1861: Failed to parse JSON encoded FHIR content: Unexpected end-of-input within/between Object entries at [line: 1, column: 30]</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1861: Failed to parse JSON encoded FHIR content: Unexpected end-of-input within/between Object entries\n at [line: 1, column: 30]"
  } ]
}
---
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1861: Failed to parse JSON encoded FHIR content: Unexpected character ('}' (code 125)): was expecting double-quote to start field name at [line: 1, column: 30]</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1861: Failed to parse JSON encoded FHIR content: Unexpected character ('}' (code 125)): was expecting double-quote to start field name\n at [line: 1, column: 30]"
  } ]
}
```

**Interpretation:** Truncated JSON: server rejected malformed JSON as expected. | Also: SECURE — Trailing comma JSON: server rejected malformed JSON as expected.

---

### 2. Metadata Manipulation

**Procedure:** three POSTs to `/Patient`: wrong `meta.versionId` type (number instead of string); `resourceType: "FakeResource"`; and a client-supplied `id` containing a null-byte marker. Behavioral classification: **VULNERABLE** only if a suspicious value is **echoed/persisted** (e.g., null-byte marker survives) or incorrect resourceType is accepted; sanitized/ignored fields with successful create can still be **SECURE**.

**Result (5):** HTTP **412**, **SECURE**.

**Reason:** Server rejected invalid meta.versionId type as expected. | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server rejected suspicious client-supplied id as expected.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/131943013</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/131943013"
  } ]
}
---
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1814: Incorrect resource type found, expected &quot;Patient&quot; but found &quot;FakeResource&quot;</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1814: Incorrect resource type found, expected \"Patient\" but found \"FakeResource\""
  } ]
}
---
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1861: Failed to parse JSON encoded FHIR content: Illegal unquoted character ((CTRL-CHAR, code 0)): has to be escaped using backslash to be included in string value at [line: 1, column: 40]</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-0450: Failed to parse request body as JSON resource. Error was: HAPI-1861: Failed to parse JSON encoded FHIR content: Illegal unquoted character ((CTRL-CHAR, code 0)): has to be escaped using backslash to be included in string value\n at [line: 1, column: 40]"
  } ]
}
```

**Interpretation:** Server rejected invalid meta.versionId type as expected. | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server rejected suspicious client-supplied id as expected.

---

### 3. Unexpected Payload Injection

**Procedure:** three POSTs to `/Patient`: (1) unknown field + `__proto__` marker; (2) duplicate JSON key for `id`; (3) `_payload` + nested object marker. Behavioral classification: **VULNERABLE** only if injected markers **persist and are retrievable** on follow-up GET.

**Result (5):** HTTP **412**, **SECURE**.

**Reason:** unknownField/__proto__ injection: server rejected the payload. | Also: SECURE — Duplicate key: server rejected ambiguous JSON as expected. | Also: SECURE — _payload/extraNested injection: server rejected the payload.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/131944407</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/131944407"
  } ]
}
---
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/131943013</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/131943013"
  } ]
}
---
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/131944407</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/131944407"
  } ]
}
```

**Interpretation:** unknownField/__proto__ injection: server rejected the payload. | Also: SECURE — Duplicate key: server rejected ambiguous JSON as expected. | Also: SECURE — _payload/extraNested injection: server rejected the payload.

---

### 4. Extension Fields Misuse

**Procedure:** POST `/Patient` with a custom `extension` that contains a unique marker, then GET `/Patient/{id}`. **VULNERABLE** only if the extension marker persists and is retrievable (covert storage channel).

**Result (5):** HTTP **201**, **VULNERABLE**.

**Reason:** Custom extension marker persisted and is retrievable (potential covert storage channel).

**Response body** (`test_result.response_body`):

```text
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "131945812",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T08:32:55.706+00:00",
    "source": "#OPVZkBE9hPzuAb0C"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\"><b>PROBE-COVERT-EXT-33E7DA0D23F4 </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "extension": [ {
    "url": "http://malicious.example/hidden",
    "valueString": "covert-ext-33e7da0d23f4"
  } ],
  "name": [ {
    "family": "Probe-covert-ext-33e7da0d23f4"
  } ]
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "131945812",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T08:32:55.706+00:00",
    "source": "#OPVZkBE9hPzuAb0C"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\"><b>PROBE-COVERT-EXT-33E7DA0D23F4 </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "extension": [ {
    "url": "http://malicious.example/hidden",
    "valueString": "covert-ext-33e7da0d23f4"
  } ],
  "name": [ {
    "family": "Probe-covert-ext-33e7da0d23f4"
  } ]
}
```

**Interpretation:** Custom extension marker persisted and is retrievable (potential covert storage channel).

---

### 5. Contained Resource Smuggling

**Procedure:** POST `/Patient` with contained `Binary.data` (base64 marker), then GET `/Patient/{id}`. **VULNERABLE** only if the contained Binary marker persists and is retrievable.

**Result (5):** HTTP **201**, **VULNERABLE**.

**Reason:** Contained Binary marker persisted and is retrievable (potential nested payload smuggling channel).

**Response body** (`test_result.response_body`):

```text
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "131945813",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T08:32:57.705+00:00",
    "source": "#tEHUDQGNfwXoFJNN"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\"><b>PROBE-COVERT-BIN-56EE82E79B0A </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "contained": [ {
    "resourceType": "Binary",
    "id": "covert",
    "contentType": "text/plain",
    "data": "Y292ZXJ0LWJpbi01NmVlODJlNzliMGE="
  } ],
  "name": [ {
    "family": "Probe-covert-bin-56ee82e79b0a"
  } ]
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "131945813",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T08:32:57.705+00:00",
    "source": "#tEHUDQGNfwXoFJNN"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\"><b>PROBE-COVERT-BIN-56EE82E79B0A </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "contained": [ {
    "resourceType": "Binary",
    "id": "covert",
    "contentType": "text/plain",
    "data": "Y292ZXJ0LWJpbi01NmVlODJlNzliMGE="
  } ],
  "name": [ {
    "family": "Probe-covert-bin-56ee82e79b0a"
  } ]
}
```

**Interpretation:** Contained Binary marker persisted and is retrievable (potential nested payload smuggling channel).

---

### 6. Encoded Hidden Data

**Procedure:** POST `/Patient` with `meta.tag.display` carrying a unicode-escaped marker, then GET `/Patient/{id}`. **VULNERABLE** only if the marker persists and is retrievable (normalization/stripping counts as **SECURE**).

**Result (5):** HTTP **412**, **SECURE**.

**Reason:** Server rejected the payload (no encoded marker persistence).

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/131943013</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/131943013"
  } ]
}
```

**Interpretation:** Server rejected the payload (no encoded marker persistence).

---

### 7. Invalid Credentials Access Test

**Procedure:** composite scenario: (1) resolve `token_endpoint` via `/.well-known/smart-configuration` and `/metadata`; if a URL exists — POST `client_credentials` with an invalid client; (2) GET `/Patient?_count=1` with invalid Basic; (3) GET with forged/malformed Bearer on `/Patient` and `/Observation` (variants include empty Bearer, non-JWT, expired-shaped token). Final outcome combines the worst results.

**Result (5):** HTTP **200**, **VULNERABLE**.

**Reason:** Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: INCONCLUSIVE — OAuth token URL not available; sub-probe skipped. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata.

**Response body** (`test_result.response_body`):

```text
No token_endpoint discovered.
---
{
  "resourceType": "Bundle",
  "id": "3a99e1b2-a56c-4c08-91a6-9c6fba26e50f",
  "meta": {
    "lastUpdated": "2026-04-22T08:33:01.673+00:00"
  },
  "type": "searchset",
  "link": [ {
    "relation": "self",
    "url": "https://hapi.fhir.org/baseR4/Patient?_count=1"
  }, {
    "relation": "next",
    "url": "https://hapi.fhir.org/baseR4?_getpages=3a99e1b2-a56c-4c08-91a6-9c6fba26e50f&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
  } ],
  "entry": [ {
    "fullUrl": "https://hapi.fhir.org/baseR4/Patient/90270587",
    "resource": {
      "resourceType": "Patient",
      "id": "90270587",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2026-02-09T18:36:41.642+00:00",
        "source": "#TGA68OVVF6gisl3w"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">NuÃ±ez <b>KARLA </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Date of birth</td><td><span>02 January 1980</span></td></tr></tbody></table></div>"
      },
      "name": [ {
        "family": "Karla",
        "given": [ "NuÃ±ez" ]
      } ],
      "gender": "female",
      "birthDate": "1980-01-02"
    },
    "search": {
      "mode": "match"
    }
  } ]
}
---
Bearer probes Patient 200 | Observation 200
---
Bearer variant → HTTP 200; Bearer variant → HTTP 200; Bearer variant → HTTP 200; 
```

**Interpretation:** Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: INCONCLUSIVE — OAuth token URL not available; sub-probe skipped. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata.

---

### 8. Open Endpoint Detection

**Procedure:** GET `/.well-known/smart-configuration` and GET `/metadata` to infer whether OAuth/SMART is advertised; then unauthenticated GET `/Patient?_count=1`. If OAuth is advertised but Patient can be read without authorization → **MISCONFIGURED**; if not advertised and read succeeds → **OPEN_POLICY**.

**Result (5):** HTTP **200**, **MISCONFIGURED**.

**Reason:** OAuth/SMART is advertised but unauthenticated Patient read succeeded (policy inconsistency).

**Response body** (`test_result.response_body`):

```text
well-known HTTP 404, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: {
  "resourceType": "Bundle",
  "id": "3a99e1b2-a56c-4c08-91a6-9c6fba26e50f",
  "meta": {
    "lastUpdated": "2026-04-22T08:33:01.673+00:00"
  },
  "type": "searchset",
  "link": [ {
    "relation": "self",
    "url": "https://hapi.fhir.org/baseR4/Patient?_count=1"
  }, {
    "relation": "next",
    "url": "https://hapi.fhir.org/baseR4?_getpages=3a99e1b2-a56c-4c08-91a6-9c6fba26e50f&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
  } ],
  "entry": [ {
    "fullUrl": "https://hapi.fhir.org/baseR4/Patient/90270587",
    "resource": {
      "resourceType": "Patient",
      "id": "90270587",
      "meta": {
        "versionId": "1",
        "lastUpdated": "2026-02-09T18:36:41.642+00:00",
        "source": "#TGA68OVVF6gisl3w"
      },
      "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">NuÃ±ez <b>KARLA </b></div><table class=\"hapiPropertyTable\"><tbody><tr><td>Date of birth</td><td><span>02 January 1980</span></td></tr></tbody></table></div>"
      },
      "name": [ {
        "family": "Karla",
        "given": [ "NuÃ±ez" ]
      } ],
      "gender": "female",
      "birthDate": "1980-01-…
```

**Interpretation:** OAuth/SMART is advertised but unauthenticated Patient read succeeded (policy inconsistency).

---

### 9. Cross-Patient Access

**Procedure:** create a victim Patient, then without authorization: GET `/Patient/{victimId}` and GET `Observation?subject=Patient/{victimId}`. If reads succeed while OAuth/SMART is advertised → **VULNERABLE**; if no OAuth is advertised → **OPEN_POLICY**.

**Result (5):** HTTP **200**, **VULNERABLE**.

**Reason:** Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization). | Also: VULNERABLE — Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization).

**Response body** (`test_result.response_body`):

```text
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

**Interpretation:** Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization). | Also: VULNERABLE — Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization).

---

### 10. Unauthorized Write / ID Tampering

**Procedure:** (1) PUT `/Patient/{id}` tampering with before/after GET verification; (2) POST `/Observation` for a victim subject plus follow-up GET `/Observation/{id}`. If write succeeds while OAuth/SMART is advertised and tampering persists → **CRITICAL VULNERABLE**.

**Result (5):** HTTP **200**, **VULNERABLE**.

**Reason:** Write succeeded without valid credentials while OAuth/SMART is advertised. Verified persistence of tampered marker in follow-up GET. | Also: VULNERABLE — Write succeeded without valid credentials while OAuth/SMART is advertised. Follow-up GET HTTP 200.

**Response body** (`test_result.response_body`):

```text
GET /Patient/{id} before PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "131945818",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T08:33:14.625+00:00",
    "source": "#OiUdWAnZXBOzssV8"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Victim-67049fb83ba0 <b>PATIENTVICTIM-67049FB83BA0 </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "name": [ {
    "family": "PatientVictim-67049fb83ba0",
    "given": [ "Victim-67049fb83ba0" ]
  } ]
}

PUT /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "131945818",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-04-22T08:33:16.624+00:00",
    "source": "#BDbEv3vlvlq1n5YF"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Tampered-4cc4eb236cf5 <b>TAMPEREDFAMILY-4CC4EB236CF5 </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "name": [ {
    "family": "TamperedFamily-4cc4eb236cf5",
    "given": [ "Tampered-4cc4eb236cf5" ]
  } ]
}

GET /Patient/{id} after PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "131945818",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-04-22T08:33:16.624+00:00",
    "source": "#BDbEv3vlvlq1n5YF"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Tampered-4cc4eb236cf5 <b>TAMPEREDFAMILY-4CC4EB236CF5 </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "name": [ {
    "family": "TamperedFamily-4cc4eb236cf5",
    "given": [ "Tampered-4cc4eb236cf5" ]
  } ]
}

POST /Observation:
{
  "resourceType": "Observation",
  "id": "131945820",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T08:33:19.776+00:00",
    "source": "#mMKZw7CNyXyCJ5kx"
  },
  "status": "final",
  "code": {
    "coding": [ {
      "system": "http://loinc.org",
      "code": "718-7"
    } ],
    "text": "SecurityTest"
  },
  "subject": {
    "reference": "Patient/131945819"
  },
  "valueString": "OwnerRefProbe-f9c78b7147c5"
}

GET /Observation/{id}:
{
  "resourceType": "Observation",
  "id": "131945820",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T08:33:19.776+00:00",
    "source": "#mMKZw7CNyXyCJ5kx"
  },
  "status": "final",
  "code": {
    "coding": [ {
      "system": "http://loinc.org",
      "code": "718-7"
    } ],
    "text": "SecurityTest"
  },
  "subject": {
    "reference": "Patient/131945819"
  },
  "valueString": "OwnerRefProbe-f9c78b7147c5"
}
```

**Interpretation:** Write succeeded without valid credentials while OAuth/SMART is advertised. Verified persistence of tampered marker in follow-up GET. | Also: VULNERABLE — Write succeeded without valid credentials while OAuth/SMART is advertised. Follow-up GET HTTP 200.

---
