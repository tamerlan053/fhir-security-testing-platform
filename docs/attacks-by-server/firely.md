# Security test run documentation: Firely

| Field | Value |
|------|----------|
| **Server** | Firely |
| **Base URL** | `https://server.fire.ly/r4` |
| **Database test run ID** | `test_run_id = 6` |
| **Run startedAt** | `2026-04-22T10:33:36.424239` |
| **Response bodies below** | As stored in PostgreSQL, column `test_result.response_body` (run 6) |

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

## Summary table: all scenarios (run 6)

| # | Scenario | HTTP | Classification | Vulnerable (legacy) | Severity |
|---|----------|------|---------------|---------------------|-------------|
| 1 | Encoded Hidden Data | 201 | VULNERABLE | yes | LOW |
| 2 | Malformed JSON Request | 201 | VULNERABLE | yes | HIGH |
| 3 | Metadata Manipulation | 201 | SECURE | no | INFO |
| 4 | Unexpected Payload Injection | 400 | SECURE | no | INFO |
| 5 | Extension Fields Misuse | 201 | VULNERABLE | yes | MEDIUM |
| 6 | Contained Resource Smuggling | 400 | SECURE | no | INFO |
| 7 | Invalid Credentials Access Test | 200 | VULNERABLE | yes | HIGH |
| 8 | Open Endpoint Detection | 200 | MISCONFIGURED | no | MEDIUM |
| 9 | Cross-Patient Access | 200 | VULNERABLE | yes | HIGH |
| 10 | Unauthorized Write / ID Tampering | 200 | VULNERABLE | yes | CRITICAL |

---

## Per-scenario detail

For **each** scenario below: what the test does (backend implementation), what was observed on the server, and a short interpretation.

### 1. Encoded Hidden Data

**Procedure:** POST `/Patient` with `meta.tag.display` carrying a unicode-escaped marker, then GET `/Patient/{id}`. **VULNERABLE** only if the marker persists and is retrievable (normalization/stripping counts as **SECURE**).

**Result (6):** HTTP **201**, **VULNERABLE**.

**Reason:** Meta.tag display marker persisted and is retrievable (potential encoded covert storage channel).

**Response body** (`test_result.response_body`):

```text
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "meta": {
    "tag": [
      {
        "code": "x",
        "display": "covert-tag-56c306fe8c01"
      }
    ],
    "versionId": "284adc48-3c95-4f37-a622-f52805f9680d",
    "lastUpdated": "2026-04-22T08:33:40.277+00:00"
  },
  "id": "a6f894fe-7077-4806-a537-810c4fdf2880"
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "a6f894fe-7077-4806-a537-810c4fdf2880",
  "meta": {
    "versionId": "284adc48-3c95-4f37-a622-f52805f9680d",
    "lastUpdated": "2026-04-22T08:33:40.277+00:00",
    "tag": [
      {
        "code": "x",
        "display": "covert-tag-56c306fe8c01"
      }
    ]
  }
}
```

**Interpretation:** Meta.tag display marker persisted and is retrievable (potential encoded covert storage channel).

---

### 2. Malformed JSON Request

**Procedure:** two POSTs to `https://server.fire.ly/r4/Patient`: (a) truncated JSON `{ "resourceType": "Patient", `; (b) JSON with a trailing comma `{ "resourceType": "Patient", }`. Behavioral classification: **VULNERABLE** only if malformed JSON leads to a **retrievable created Patient** (POST→GET verification); `400/412/422/...` rejection → **SECURE**.

**Result (6):** HTTP **201**, **VULNERABLE**.

**Reason:** Trailing comma JSON: malformed JSON resulted in a retrievable Patient resource (partial parsing / permissive parser). | Also: SECURE — Truncated JSON: server rejected malformed JSON as expected.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "id": "b2980aad-3fda-42e5-bc33-743e2151e621",
  "meta": {
    "versionId": "433ab277-2ec1-4b14-b7f3-9c1968d420e3",
    "lastUpdated": "2026-04-22T08:33:36.687834+00:00"
  },
  "issue": [
    {
      "severity": "error",
      "code": "invalid",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5000"
          }
        ],
        "text": "Expected start of a property name or value, but instead reached end of data. LineNumber: 0 | BytePositionInLine: 29."
      }
    },
    {
      "severity": "warning",
      "code": "not-supported",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5003"
          }
        ],
        "text": "Argument is not supported"
      },
      "diagnostics": "/Patient"
    }
  ]
}
---
Trailing comma JSON POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "680ee45f-faac-44e8-9d5b-a08b2225fd81",
  "meta": {
    "versionId": "e16eeb63-b84f-4946-bd9c-5c38eeaa8f0a",
    "lastUpdated": "2026-04-22T08:33:37.295+00:00"
  }
}

Follow-up GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "680ee45f-faac-44e8-9d5b-a08b2225fd81",
  "meta": {
    "versionId": "e16eeb63-b84f-4946-bd9c-5c38eeaa8f0a",
    "lastUpdated": "2026-04-22T08:33:37.295+00:00"
  }
}
```

**Interpretation:** Trailing comma JSON: malformed JSON resulted in a retrievable Patient resource (partial parsing / permissive parser). | Also: SECURE — Truncated JSON: server rejected malformed JSON as expected.

---

### 3. Metadata Manipulation

**Procedure:** three POSTs to `/Patient`: wrong `meta.versionId` type (number instead of string); `resourceType: "FakeResource"`; and a client-supplied `id` containing a null-byte marker. Behavioral classification: **VULNERABLE** only if a suspicious value is **echoed/persisted** (e.g., null-byte marker survives) or incorrect resourceType is accepted; sanitized/ignored fields with successful create can still be **SECURE**.

**Result (6):** HTTP **201**, **SECURE**.

**Reason:** Server did not persist/echo the invalid meta.versionId (client meta appears ignored/sanitized). | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server rejected suspicious client-supplied id as expected.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "meta": {
    "versionId": "559b71fe-3e81-413f-addb-d38ffcd3185f",
    "lastUpdated": "2026-04-22T08:33:37.869+00:00"
  },
  "id": "08dc346f-282c-4ec4-afb9-eb307c2f0ae6"
}
---
{
  "resourceType": "OperationOutcome",
  "id": "09b739cd-2d16-44a7-bfce-3c33c92306af",
  "meta": {
    "versionId": "82f24887-0311-48bf-b16a-02e2ef0613ca",
    "lastUpdated": "2026-04-22T08:33:38.0273811+00:00"
  },
  "issue": [
    {
      "severity": "error",
      "code": "incomplete",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "4000"
          }
        ],
        "text": "Unable to resolve reference to profile 'http://hl7.org/fhir/StructureDefinition/FakeResource'."
      },
      "expression": [
        "FakeResource"
      ]
    }
  ]
}
---
{
  "resourceType": "OperationOutcome",
  "id": "9b53d441-e66b-4743-8683-c55c605bda6a",
  "meta": {
    "versionId": "b7bbd9bd-c9ed-4acd-b6db-185812a32142",
    "lastUpdated": "2026-04-22T08:33:38.1059182+00:00"
  },
  "issue": [
    {
      "severity": "error",
      "code": "invalid",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5000"
          }
        ],
        "text": "'0x00' is invalid within a JSON string. The string should be correctly escaped. LineNumber: 0 | BytePositionInLine: 39."
      }
    },
    {
      "severity": "warning",
      "code": "not-supported",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5003"
          }
        ],
        "text": "Argument is not supported"
      },
      "diagnostics": "/Patient"
    }
  ]
}
```

**Interpretation:** Server did not persist/echo the invalid meta.versionId (client meta appears ignored/sanitized). | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server rejected suspicious client-supplied id as expected.

---

### 4. Unexpected Payload Injection

**Procedure:** three POSTs to `/Patient`: (1) unknown field + `__proto__` marker; (2) duplicate JSON key for `id`; (3) `_payload` + nested object marker. Behavioral classification: **VULNERABLE** only if injected markers **persist and are retrievable** on follow-up GET.

**Result (6):** HTTP **400**, **SECURE**.

**Reason:** unknownField/__proto__ injection: server rejected the payload. | Also: SECURE — Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed. | Also: SECURE — _payload/extraNested injection: server rejected the payload.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "id": "44420ae0-1e6b-48a4-87d0-11c8ab828ec2",
  "meta": {
    "versionId": "ce23ab2e-190a-484d-9a2c-ec57ec69e022",
    "lastUpdated": "2026-04-22T08:33:38.1457698+00:00"
  },
  "issue": [
    {
      "severity": "error",
      "code": "exception",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5006"
          }
        ],
        "text": "Type checking the data: Encountered unknown element 'unknownField' at location 'Patient.unknownField[0]' while parsing"
      }
    },
    {
      "severity": "error",
      "code": "exception",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5006"
          }
        ],
        "text": "Type checking the data: Encountered unknown element '_proto__' at location 'Patient._proto__[0]' while parsing"
      }
    }
  ]
}
---
Duplicate key POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "0e34e6a2-09ad-426a-9944-ee58bdd87321",
  "meta": {
    "versionId": "51255503-0b0b-4d09-9fa2-a4a9fa7a10a5",
    "lastUpdated": "2026-04-22T08:33:38.213+00:00"
  }
}

Follow-up GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "0e34e6a2-09ad-426a-9944-ee58bdd87321",
  "meta": {
    "versionId": "51255503-0b0b-4d09-9fa2-a4a9fa7a10a5",
    "lastUpdated": "2026-04-22T08:33:38.213+00:00"
  }
}
---
{
  "resourceType": "OperationOutcome",
  "id": "c8d4de22-d8c1-4a9d-847d-b6af1d66a9d2",
  "meta": {
    "versionId": "5ba078c8-fe5f-4ff3-aede-7fb072e213c1",
    "lastUpdated": "2026-04-22T08:33:38.5440272+00:00"
  },
  "issue": [
    {
      "severity": "error",
      "code": "exception",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5006"
          }
        ],
        "text": "Type checking the data: Encountered unknown element 'payload' at location 'Patient.payload[0]' while parsing"
      }
    },
    {
      "severity": "error",
      "code": "exception",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "5006"
          }
        ],
        "text": "Type checking the data: Encountered unknown element 'extraNested' at location 'Patient.extraNested[0]' while parsing"
      }
    }
  ]
}
```

**Interpretation:** unknownField/__proto__ injection: server rejected the payload. | Also: SECURE — Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed. | Also: SECURE — _payload/extraNested injection: server rejected the payload.

---

### 5. Extension Fields Misuse

**Procedure:** POST `/Patient` with a custom `extension` that contains a unique marker, then GET `/Patient/{id}`. **VULNERABLE** only if the extension marker persists and is retrievable (covert storage channel).

**Result (6):** HTTP **201**, **VULNERABLE**.

**Reason:** Custom extension marker persisted and is retrievable (potential covert storage channel).

**Response body** (`test_result.response_body`):

```text
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-ext-eb09f070cbb0"
    }
  ],
  "name": [
    {
      "family": "Probe-covert-ext-eb09f070cbb0"
    }
  ],
  "id": "b158cf1c-ac74-436b-8a9c-29a58a091c93",
  "meta": {
    "versionId": "6ef27992-1fa4-472f-abb2-424e6c181886",
    "lastUpdated": "2026-04-22T08:33:39.654+00:00"
  }
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "b158cf1c-ac74-436b-8a9c-29a58a091c93",
  "meta": {
    "versionId": "6ef27992-1fa4-472f-abb2-424e6c181886",
    "lastUpdated": "2026-04-22T08:33:39.654+00:00"
  },
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-ext-eb09f070cbb0"
    }
  ],
  "name": [
    {
      "family": "Probe-covert-ext-eb09f070cbb0"
    }
  ]
}
```

**Interpretation:** Custom extension marker persisted and is retrievable (potential covert storage channel).

---

### 6. Contained Resource Smuggling

**Procedure:** POST `/Patient` with contained `Binary.data` (base64 marker), then GET `/Patient/{id}`. **VULNERABLE** only if the contained Binary marker persists and is retrievable.

**Result (6):** HTTP **400**, **SECURE**.

**Reason:** Server rejected the payload (no contained-resource persistence).

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "id": "0d8b724e-d9da-4745-aaa9-f8126bdddd6a",
  "meta": {
    "versionId": "37516eb1-55be-4a39-b739-6a255d2a25d1",
    "lastUpdated": "2026-04-22T08:33:39.9123832+00:00"
  },
  "issue": [
    {
      "extension": [
        {
          "url": "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line",
          "valueInteger": 1
        },
        {
          "url": "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-col",
          "valueInteger": 2
        },
        {
          "url": "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-source",
          "valueString": "InvariantValidator"
        }
      ],
      "severity": "error",
      "code": "invariant",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "1012"
          }
        ],
        "text": "Instance failed constraint dom-3 \"If the resource is contained in another resource, it SHALL be referred to from elsewhere in the resource or SHALL refer to the containing resource\""
      },
      "diagnostics": "ElementDefinition trace: Patient",
      "expression": [
        "Patient"
      ]
    }
  ]
}
```

**Interpretation:** Server rejected the payload (no contained-resource persistence).

---

### 7. Invalid Credentials Access Test

**Procedure:** composite scenario: (1) resolve `token_endpoint` via `/.well-known/smart-configuration` and `/metadata`; if a URL exists — POST `client_credentials` with an invalid client; (2) GET `/Patient?_count=1` with invalid Basic; (3) GET with forged/malformed Bearer on `/Patient` and `/Observation` (variants include empty Bearer, non-JWT, expired-shaped token). Final outcome combines the worst results.

**Result (6):** HTTP **200**, **VULNERABLE**.

**Reason:** Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: INCONCLUSIVE — OAuth token URL not available; sub-probe skipped. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata. | Also: VULNERABLE — Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata.

**Response body** (`test_result.response_body`):

```text
No token_endpoint discovered.
---
{
  "resourceType": "Bundle",
  "type": "searchset",
  "timestamp": "2026-04-22T08:33:49.204+00:00",
  "meta": {
    "lastUpdated": "2026-04-22T08:33:49.204+00:00",
    "versionId": "818c878a-e89d-4016-bbc8-34d8fad36ff1"
  },
  "entry": [
    {
      "fullUrl": "https://server.fire.ly/r4/Patient/a6f894fe-7077-4806-a537-810c4fdf2880",
      "search": {
        "mode": "match"
      },
      "resource": {
        "resourceType": "Patient",
        "id": "a6f894fe-7077-4806-a537-810c4fdf2880",
        "meta": {
          "versionId": "284adc48-3c95-4f37-a622-f52805f9680d",
          "lastUpdated": "2026-04-22T08:33:40.277+00:00",
          "tag": [
            {
              "code": "x",
              "display": "covert-tag-56c306fe8c01"
            }
          ]
        }
      }
    }
  ],
  "link": [
    {
      "relation": "self",
      "url": "https://server.fire.ly/r4/Patient?_total=none&_count=1&_skip=0"
    },
    {
      "relation": "next",
      "url": "https://server.fire.ly/r4/?q=CfDJ8LjzO31FTPZ7oJjS9LaN61mfGFoYUCKvHDf3sXYx-KhXezWfkIvPnEM5uwA7tCulqDCmBHsyXdsAnTkOVNTSr1Xe_JIott904nrO1fwpeI16sXE5njcN8gPA2AX6ZD2LKMk8rDl33FZfeLEqqeqUxsyL2fcbYzLcvTpFR95wiXTJ3z6cb_yGi-_OVRwkL25lBT31kkw9QgGsrI8vqTBI8f9DqX86gM7xmFIciZm2mZ1s"
    }
  ],
  "id": "be4a7575-5cfb-43ef-91ff-4d0e3d2207f5"
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

**Result (6):** HTTP **200**, **MISCONFIGURED**.

**Reason:** OAuth/SMART is advertised but unauthenticated Patient read succeeded (policy inconsistency).

**Response body** (`test_result.response_body`):

```text
well-known HTTP 501, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: {
  "resourceType": "Bundle",
  "type": "searchset",
  "timestamp": "2026-04-22T08:33:50.011+00:00",
  "meta": {
    "lastUpdated": "2026-04-22T08:33:50.011+00:00",
    "versionId": "3e63817e-bc80-464e-97bb-591e7e461e01"
  },
  "entry": [
    {
      "fullUrl": "https://server.fire.ly/r4/Patient/a6f894fe-7077-4806-a537-810c4fdf2880",
      "search": {
        "mode": "match"
      },
      "resource": {
        "resourceType": "Patient",
        "id": "a6f894fe-7077-4806-a537-810c4fdf2880",
        "meta": {
          "versionId": "284adc48-3c95-4f37-a622-f52805f9680d",
          "lastUpdated": "2026-04-22T08:33:40.277+00:00",
          "tag": [
            {
              "code": "x",
              "display": "covert-tag-56c306fe8c01"
            }
          ]
        }
      }
    }
  ],
  "link": [
    {
      "relation": "self",
      "url": "https://server.fire.ly/r4/Patient?_total=none&_count=1&_skip=0"
    },
    {
      "relation": "next",
      "url": "https://server.fire.ly/r4/?q=CfDJ8LjzO31FTPZ7oJjS9LaN61nK0jg1OF0cSdRchmIuJHgfhUhBk6QqiNF1HeOlllb27BjoqF7gYHwqVJUXcE14xo26fhkV1kTdKPOUPJri5Eb6SYWhIp4xKfn4kst11pyZtarcxohd8gAGXP-DG9FocI7ZK2SZXNflHtG-QoZBhO1rHMILi8_HDq-IvMyJi7U…
```

**Interpretation:** OAuth/SMART is advertised but unauthenticated Patient read succeeded (policy inconsistency).

---

### 9. Cross-Patient Access

**Procedure:** create a victim Patient, then without authorization: GET `/Patient/{victimId}` and GET `Observation?subject=Patient/{victimId}`. If reads succeed while OAuth/SMART is advertised → **VULNERABLE**; if no OAuth is advertised → **OPEN_POLICY**.

**Result (6):** HTTP **200**, **VULNERABLE**.

**Reason:** Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization). | Also: VULNERABLE — Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization).

**Response body** (`test_result.response_body`):

```text
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

**Interpretation:** Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization). | Also: VULNERABLE — Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization).

---

### 10. Unauthorized Write / ID Tampering

**Procedure:** (1) PUT `/Patient/{id}` tampering with before/after GET verification; (2) POST `/Observation` for a victim subject plus follow-up GET `/Observation/{id}`. If write succeeds while OAuth/SMART is advertised and tampering persists → **CRITICAL VULNERABLE**.

**Result (6):** HTTP **200**, **VULNERABLE**.

**Reason:** Write succeeded without valid credentials while OAuth/SMART is advertised. Verified persistence of tampered marker in follow-up GET. | Also: VULNERABLE — Write succeeded without valid credentials while OAuth/SMART is advertised. Follow-up GET HTTP 200.

**Response body** (`test_result.response_body`):

```text
GET /Patient/{id} before PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "db2ab7fd-b238-4d74-8752-e6204f8c586a",
  "meta": {
    "versionId": "da88c969-d66d-4903-bbc1-b7a501ab0e94",
    "lastUpdated": "2026-04-22T08:33:52.643+00:00"
  },
  "name": [
    {
      "family": "PatientVictim-43226de782d8",
      "given": [
        "Victim-43226de782d8"
      ]
    }
  ]
}

PUT /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "db2ab7fd-b238-4d74-8752-e6204f8c586a",
  "name": [
    {
      "family": "TamperedFamily-3bb39841438d",
      "given": [
        "Tampered-3bb39841438d"
      ]
    }
  ],
  "meta": {
    "versionId": "ef516245-effb-4d7d-90b3-22b7265503f9",
    "lastUpdated": "2026-04-22T08:33:53.206+00:00"
  }
}

GET /Patient/{id} after PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "db2ab7fd-b238-4d74-8752-e6204f8c586a",
  "meta": {
    "versionId": "ef516245-effb-4d7d-90b3-22b7265503f9",
    "lastUpdated": "2026-04-22T08:33:53.206+00:00"
  },
  "name": [
    {
      "family": "TamperedFamily-3bb39841438d",
      "given": [
        "Tampered-3bb39841438d"
      ]
    }
  ]
}

POST /Observation:
{
  "resourceType": "Observation",
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
    "reference": "https://server.fire.ly/r4/Patient/a0b7e95f-339a-49f4-892b-1f6bcde0f602"
  },
  "valueString": "OwnerRefProbe-30b5a79f9564",
  "id": "70005201-732d-426f-bcef-3db5e15d7da4",
  "meta": {
    "versionId": "ff7dcf8f-97e7-417d-8a80-d8c9e14e50b2",
    "lastUpdated": "2026-04-22T08:33:53.946+00:00"
  }
}

GET /Observation/{id}:
{
  "resourceType": "Observation",
  "id": "70005201-732d-426f-bcef-3db5e15d7da4",
  "meta": {
    "versionId": "ff7dcf8f-97e7-417d-8a80-d8c9e14e50b2",
    "lastUpdated": "2026-04-22T08:33:53.946+00:00"
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
    "reference": "https://server.fire.ly/r4/Patient/a0b7e95f-339a-49f4-892b-1f6bcde0f602"
  },
  "valueString": "OwnerRefProbe-30b5a79f9564"
}
```

**Interpretation:** Write succeeded without valid credentials while OAuth/SMART is advertised. Verified persistence of tampered marker in follow-up GET. | Also: VULNERABLE — Write succeeded without valid credentials while OAuth/SMART is advertised. Follow-up GET HTTP 200.

---
