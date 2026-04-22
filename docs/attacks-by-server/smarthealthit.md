# Security test run documentation: Smarthealthit

| Field | Value |
|------|----------|
| **Server** | Smarthealthit |
| **Base URL** | `https://r4.smarthealthit.org` |
| **Database test run ID** | `test_run_id = 7` |
| **Run startedAt** | `2026-04-22T10:34:02.507433` |
| **Response bodies below** | As stored in PostgreSQL, column `test_result.response_body` (run 7) |

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

## Summary table: all scenarios (run 7)

| # | Scenario | HTTP | Classification | Vulnerable (legacy) | Severity |
|---|----------|------|---------------|---------------------|-------------|
| 1 | Malformed JSON Request | 400 | SECURE | no | INFO |
| 2 | Metadata Manipulation | 201 | SECURE | no | INFO |
| 3 | Unauthorized Write / ID Tampering | 200 | VULNERABLE | yes | CRITICAL |
| 4 | Unexpected Payload Injection | 201 | SECURE | no | INFO |
| 5 | Extension Fields Misuse | 201 | VULNERABLE | yes | MEDIUM |
| 6 | Contained Resource Smuggling | 201 | VULNERABLE | yes | MEDIUM |
| 7 | Encoded Hidden Data | 201 | SECURE | no | INFO |
| 8 | Invalid Credentials Access Test | 200 | VULNERABLE | yes | HIGH |
| 9 | Open Endpoint Detection | 200 | MISCONFIGURED | no | MEDIUM |
| 10 | Cross-Patient Access | 200 | VULNERABLE | yes | HIGH |

---

## Per-scenario detail

For **each** scenario below: what the test does (backend implementation), what was observed on the server, and a short interpretation.

### 1. Malformed JSON Request

**Procedure:** two POSTs to `https://r4.smarthealthit.org/Patient`: (a) truncated JSON `{ "resourceType": "Patient", `; (b) JSON with a trailing comma `{ "resourceType": "Patient", }`. Behavioral classification: **VULNERABLE** only if malformed JSON leads to a **retrievable created Patient** (POST→GET verification); `400/412/422/...` rejection → **SECURE**.

**Result (7):** HTTP **400**, **SECURE**.

**Reason:** Truncated JSON: server rejected malformed JSON as expected. | Also: SECURE — Trailing comma JSON: server rejected malformed JSON as expected.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "processing",
      "diagnostics": "Failed to parse request body as JSON resource. Error was: Failed to parse JSON encoded FHIR content: java.io.EOFException: End of input at line 1 column 30 path $.resourceType"
    }
  ]
}
---
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "processing",
      "diagnostics": "Failed to parse request body as JSON resource. Error was: Failed to parse JSON encoded FHIR content: com.google.gson.stream.MalformedJsonException: Expected name at line 1 column 31 path $.resourceType"
    }
  ]
}
```

**Interpretation:** Truncated JSON: server rejected malformed JSON as expected. | Also: SECURE — Trailing comma JSON: server rejected malformed JSON as expected.

---

### 2. Metadata Manipulation

**Procedure:** three POSTs to `/Patient`: wrong `meta.versionId` type (number instead of string); `resourceType: "FakeResource"`; and a client-supplied `id` containing a null-byte marker. Behavioral classification: **VULNERABLE** only if a suspicious value is **echoed/persisted** (e.g., null-byte marker survives) or incorrect resourceType is accepted; sanitized/ignored fields with successful create can still be **SECURE**.

**Result (7):** HTTP **201**, **SECURE**.

**Reason:** Server did not persist/echo the invalid meta.versionId (client meta appears ignored/sanitized). | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server did not reflect/persist the null-byte marker (id appears sanitized/rewritten). Assigned id: 3678623.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "3678622",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:03.136-04:00"
  }
}
---
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "processing",
      "diagnostics": "Failed to parse request body as JSON resource. Error was: Incorrect resource type found, expected \"Patient\" but found \"FakeResource\""
    }
  ]
}
---
{
  "resourceType": "Patient",
  "id": "3678623",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:03.397-04:00"
  }
}
```

**Interpretation:** Server did not persist/echo the invalid meta.versionId (client meta appears ignored/sanitized). | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server did not reflect/persist the null-byte marker (id appears sanitized/rewritten). Assigned id: 3678623.

---

### 3. Unauthorized Write / ID Tampering

**Procedure:** (1) PUT `/Patient/{id}` tampering with before/after GET verification; (2) POST `/Observation` for a victim subject plus follow-up GET `/Observation/{id}`. If write succeeds while OAuth/SMART is advertised and tampering persists → **CRITICAL VULNERABLE**.

**Result (7):** HTTP **200**, **VULNERABLE**.

**Reason:** Write succeeded without valid credentials while OAuth/SMART is advertised. Verified persistence of tampered marker in follow-up GET. | Also: VULNERABLE — Write succeeded without valid credentials while OAuth/SMART is advertised. Follow-up GET HTTP 200.

**Response body** (`test_result.response_body`):

```text
GET /Patient/{id} before PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678633",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:09.557-04:00"
  },
  "name": [
    {
      "family": "PatientVictim-29cff3b07b32",
      "given": [
        "Victim-29cff3b07b32"
      ]
    }
  ]
}

PUT /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678633",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-04-22T04:34:09.800-04:00"
  },
  "name": [
    {
      "family": "TamperedFamily-cdad8b58655d",
      "given": [
        "Tampered-cdad8b58655d"
      ]
    }
  ]
}

GET /Patient/{id} after PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678633",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-04-22T04:34:09.800-04:00"
  },
  "name": [
    {
      "family": "TamperedFamily-cdad8b58655d",
      "given": [
        "Tampered-cdad8b58655d"
      ]
    }
  ]
}

POST /Observation:
{
  "resourceType": "Observation",
  "id": "3678635",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:10.528-04:00"
  },
  "status": "final",
  "code": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "718-7"
      }
    ],
    "text": "SecurityTest"
  },
  "subject": {
    "reference": "Patient/3678634"
  },
  "valueString": "OwnerRefProbe-c0b9d5636e75"
}

GET /Observation/{id}:
{
  "resourceType": "Observation",
  "id": "3678635",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:10.528-04:00"
  },
  "status": "final",
  "code": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "718-7"
      }
    ],
    "text": "SecurityTest"
  },
  "subject": {
    "reference": "Patient/3678634"
  },
  "valueString": "OwnerRefProbe-c0b9d5636e75"
}
```

**Interpretation:** Write succeeded without valid credentials while OAuth/SMART is advertised. Verified persistence of tampered marker in follow-up GET. | Also: VULNERABLE — Write succeeded without valid credentials while OAuth/SMART is advertised. Follow-up GET HTTP 200.

---

### 4. Unexpected Payload Injection

**Procedure:** three POSTs to `/Patient`: (1) unknown field + `__proto__` marker; (2) duplicate JSON key for `id`; (3) `_payload` + nested object marker. Behavioral classification: **VULNERABLE** only if injected markers **persist and are retrievable** on follow-up GET.

**Result (7):** HTTP **201**, **SECURE**.

**Reason:** unknownField/__proto__ injection: marker not present on follow-up GET (server likely ignored/stripped unexpected fields). | Also: SECURE — Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed. | Also: SECURE — _payload/extraNested injection: marker not present on follow-up GET (server likely ignored/stripped unexpected fields).

**Response body** (`test_result.response_body`):

```text
unknownField/__proto__ injection POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "3678624",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:03.649-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}

Follow-up GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678624",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:03.649-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}
---
Duplicate key POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "3678625",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:03.898-04:00"
  }
}

Follow-up GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678625",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:03.898-04:00"
  }
}
---
_payload/extraNested injection POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "3678626",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.148-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}

Follow-up GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678626",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.148-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}
```

**Interpretation:** unknownField/__proto__ injection: marker not present on follow-up GET (server likely ignored/stripped unexpected fields). | Also: SECURE — Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed. | Also: SECURE — _payload/extraNested injection: marker not present on follow-up GET (server likely ignored/stripped unexpected fields).

---

### 5. Extension Fields Misuse

**Procedure:** POST `/Patient` with a custom `extension` that contains a unique marker, then GET `/Patient/{id}`. **VULNERABLE** only if the extension marker persists and is retrievable (covert storage channel).

**Result (7):** HTTP **201**, **VULNERABLE**.

**Reason:** Custom extension marker persisted and is retrievable (potential covert storage channel).

**Response body** (`test_result.response_body`):

```text
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "3678627",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.399-04:00"
  },
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-ext-565f8236f6dc"
    }
  ],
  "name": [
    {
      "family": "Probe-covert-ext-565f8236f6dc"
    }
  ]
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678627",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.399-04:00"
  },
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-ext-565f8236f6dc"
    }
  ],
  "name": [
    {
      "family": "Probe-covert-ext-565f8236f6dc"
    }
  ]
}
```

**Interpretation:** Custom extension marker persisted and is retrievable (potential covert storage channel).

---

### 6. Contained Resource Smuggling

**Procedure:** POST `/Patient` with contained `Binary.data` (base64 marker), then GET `/Patient/{id}`. **VULNERABLE** only if the contained Binary marker persists and is retrievable.

**Result (7):** HTTP **201**, **VULNERABLE**.

**Reason:** Contained Binary marker persisted and is retrievable (potential nested payload smuggling channel).

**Response body** (`test_result.response_body`):

```text
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "3678628",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.649-04:00"
  },
  "name": [
    {
      "family": "Probe-covert-bin-3450b94b4a41"
    }
  ]
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678628",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.649-04:00"
  },
  "name": [
    {
      "family": "Probe-covert-bin-3450b94b4a41"
    }
  ]
}
```

**Interpretation:** Contained Binary marker persisted and is retrievable (potential nested payload smuggling channel).

---

### 7. Encoded Hidden Data

**Procedure:** POST `/Patient` with `meta.tag.display` carrying a unicode-escaped marker, then GET `/Patient/{id}`. **VULNERABLE** only if the marker persists and is retrievable (normalization/stripping counts as **SECURE**).

**Result (7):** HTTP **201**, **SECURE**.

**Reason:** Meta.tag marker was not present on follow-up GET (server likely normalized/stripped it).

**Response body** (`test_result.response_body`):

```text
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "3678629",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.914-04:00",
    "tag": [
      {
        "code": "x",
        "display": "covert-tag-54ca360b6d18"
      }
    ]
  }
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3678629",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-22T04:34:04.914-04:00",
    "tag": [
      {
        "code": "x",
        "display": "Secret"
      }
    ]
  }
}
```

**Interpretation:** Meta.tag marker was not present on follow-up GET (server likely normalized/stripped it).

---

### 8. Invalid Credentials Access Test

**Procedure:** composite scenario: (1) resolve `token_endpoint` via `/.well-known/smart-configuration` and `/metadata`; if a URL exists — POST `client_credentials` with an invalid client; (2) GET `/Patient?_count=1` with invalid Basic; (3) GET with forged/malformed Bearer on `/Patient` and `/Observation` (variants include empty Bearer, non-JWT, expired-shaped token). Final outcome combines the worst results.

**Result (7):** HTTP **200**, **VULNERABLE**.

**Reason:** Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: INCONCLUSIVE — OAuth token URL not available; sub-probe skipped. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata.

**Response body** (`test_result.response_body`):

```text
No token_endpoint discovered.
---
{
  "resourceType": "Bundle",
  "id": "a23dd5c7-9c41-48ec-b3f4-47726944f9c4",
  "meta": {
    "lastUpdated": "2026-04-22T04:34:06.427-04:00"
  },
  "type": "searchset",
  "link": [
    {
      "relation": "self",
      "url": "https://r4.smarthealthit.org/Patient?_count=1"
    },
    {
      "relation": "next",
      "url": "https://r4.smarthealthit.org?_getpages=a23dd5c7-9c41-48ec-b3f4-47726944f9c4&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
    }
  ],
  "entry": [
    {
      "fullUrl": "https://r4.smarthealthit.org/Patient/d4fb3bba-73a9-4b82-a0bc-678d47f386b4",
      "resource": {
        "resourceType": "Patient",
        "id": "d4fb3bba-73a9-4b82-a0bc-678d47f386b4",
        "meta": {
          "versionId": "4",
          "lastUpdated": "2026-03-25T13:26:50.088-04:00",
          "tag": [
            {
              "system": "https://smarthealthit.org/tags",
              "code": "synthea-5-2019"
            },
            {
              "system": "https://aletheamedical.com/fhir/tags",
              "code": "smart-launch-verified",
              "display": "Verified via Alethea SMART Launch"
            }
          ]
        },
        "text": {
          "status": "generated",
          "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\">Generated by <a href=\"https://github.com/synthetichealth/synthea\">Synthea</a>.Version identifier: v2.4.0-100-g26a4b936\n .   Person seed: 8163518349110178323  Population seed: 1559319163074</div>"
        },
      …
---
Bearer probes Patient 200 | Observation 200
---
Bearer variant → HTTP 200; Bearer variant → HTTP 200; Bearer variant → HTTP 200; 
```

**Interpretation:** Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: INCONCLUSIVE — OAuth token URL not available; sub-probe skipped. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata.

---

### 9. Open Endpoint Detection

**Procedure:** GET `/.well-known/smart-configuration` and GET `/metadata` to infer whether OAuth/SMART is advertised; then unauthenticated GET `/Patient?_count=1`. If OAuth is advertised but Patient can be read without authorization → **MISCONFIGURED**; if not advertised and read succeeds → **OPEN_POLICY**.

**Result (7):** HTTP **200**, **MISCONFIGURED**.

**Reason:** OAuth/SMART is advertised but unauthenticated Patient read succeeded (policy inconsistency).

**Response body** (`test_result.response_body`):

```text
well-known HTTP 404, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: {
  "resourceType": "Bundle",
  "id": "a23dd5c7-9c41-48ec-b3f4-47726944f9c4",
  "meta": {
    "lastUpdated": "2026-04-22T04:34:06.427-04:00"
  },
  "type": "searchset",
  "link": [
    {
      "relation": "self",
      "url": "https://r4.smarthealthit.org/Patient?_count=1"
    },
    {
      "relation": "next",
      "url": "https://r4.smarthealthit.org?_getpages=a23dd5c7-9c41-48ec-b3f4-47726944f9c4&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
    }
  ],
  "entry": [
    {
      "fullUrl": "https://r4.smarthealthit.org/Patient/d4fb3bba-73a9-4b82-a0bc-678d47f386b4",
      "resource": {
        "resourceType": "Patient",
        "id": "d4fb3bba-73a9-4b82-a0bc-678d47f386b4",
        "meta": {
          "versionId": "4",
          "lastUpdated": "2026-03-25T13:26:50.088-04:00",
          "tag": [
            {
              "system": "https://smarthealthit.org/tags",
              "code": "synthea-5-2019"
            },
            {
              "system": "https://aletheamedical.com/fhir/tags",
              "code": "smart-launch-verified",
              "display": "Verified via Alethea SMART Launch"
            }
          ]
        },
        "text": {
          "…
```

**Interpretation:** OAuth/SMART is advertised but unauthenticated Patient read succeeded (policy inconsistency).

---

### 10. Cross-Patient Access

**Procedure:** create a victim Patient, then without authorization: GET `/Patient/{victimId}` and GET `Observation?subject=Patient/{victimId}`. If reads succeed while OAuth/SMART is advertised → **VULNERABLE**; if no OAuth is advertised → **OPEN_POLICY**.

**Result (7):** HTTP **200**, **VULNERABLE**.

**Reason:** Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization). | Also: VULNERABLE — Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization).

**Response body** (`test_result.response_body`):

```text
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

**Interpretation:** Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization). | Also: VULNERABLE — Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization).

---
