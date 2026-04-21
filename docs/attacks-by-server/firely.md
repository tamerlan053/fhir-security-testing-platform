# Security test run documentation: Firely Server

| Field | Value |
|------|----------|
| **Server** | Firely |
| **Base URL** | `https://server.fire.ly/r4` |
| **Database test run ID** | `test_run_id = 32` |
| **Summary metrics source** | `reports/analysis-runs-31-34-summary.tsv` |
| **Response bodies below** | As stored in PostgreSQL, column `test_result.response_body` (run 32) |

## Classification legend

| Classification | Meaning |
|---------------|--------|
| **SECURE** | Expected rejection or correct behavior. |
| **VULNERABLE** | Confirmed risk. |
| **OPEN_POLICY** | Consistent with public policy (no advertised OAuth). |
| **MISCONFIGURED** | OAuth/SMART is advertised, but anonymous access is inconsistent with that. |
| **INCONCLUSIVE** | No conclusion possible. |

---

## Summary table: all scenarios (run 32)

| # | Scenario | HTTP | Classification | Vulnerable (legacy) | Severity |
|---|----------|------|---------------|---------------------|-------------|
| 1 | Malformed JSON Request | 201 | VULNERABLE | yes | HIGH |
| 2 | Metadata Manipulation | 201 | VULNERABLE | yes | HIGH |
| 3 | Unexpected Payload Injection | 201 | VULNERABLE | yes | HIGH |
| 4 | Extension Fields Misuse | 201 | VULNERABLE | yes | HIGH |
| 5 | Contained Resource Smuggling | 400 | SECURE | no | INFO |
| 6 | Encoded Hidden Data | 201 | VULNERABLE | yes | HIGH |
| 7 | Invalid Credentials Access Test | 200 | VULNERABLE | yes | HIGH |
| 8 | Open Endpoint Detection | 200 | MISCONFIGURED | no | MEDIUM |
| 9 | Cross-Patient Access | 200 | VULNERABLE | yes | HIGH |
| 10 | Unauthorized Write / ID Tampering | 200 | VULNERABLE | yes | CRITICAL |

---

## Per-scenario detail

### 1. Malformed JSON Request (`MalformedJsonRequestAttack`, order 10)

**Procedure:** POST `/Patient` with truncated JSON and a trailing comma; **200/201** on any step → **VULNERABLE**.

**Result (32):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "id": "6ba48e7c-834b-44ad-9a59-b7116739650d",
  "meta": {
    "versionId": "64f4818a-6774-46e6-b906-06a92e42adae",
    "lastUpdated": "2026-04-21T10:49:50.628634+00:00"
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
{
  "resourceType": "Patient",
  "id": "9502299e-6d83-4cd6-bed7-a6e4b7cba6c2",
  "meta": {
    "versionId": "75a5a181-4159-4391-9e12-1af6dfc3be1d",
    "lastUpdated": "2026-04-21T10:49:51.240+00:00"
  }
}
```

**Interpretation:** the server returns create/processing success where strict rejection of invalid JSON is expected — typical of a permissive test sandbox or weak parser↔HTTP status coupling.

---

### 2. Metadata Manipulation (`MetadataManipulationAttack`, order 20)

**Procedure:** three POSTs: wrong `meta.versionId`, fake `resourceType`, id/identifier manipulation.

**Result (32):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "meta": {
    "versionId": "25055b22-ca6d-4ab5-bd09-439efaa767c6",
    "lastUpdated": "2026-04-21T10:49:51.672+00:00"
  },
  "id": "ae6d1f01-a15b-4692-a704-57d6e64f7718"
}
---
{
  "resourceType": "OperationOutcome",
  "id": "5a095df4-c45f-4ca1-8a57-ce12e01275fd",
  "meta": {
    "versionId": "4b6355d5-a787-4b88-af36-af86913e9ccc",
    "lastUpdated": "2026-04-21T10:49:51.9863626+00:00"
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
  "id": "41471104-f90c-47b4-8517-ffed452fc52b",
  "meta": {
    "versionId": "b7f5b256-b05d-40ff-b59d-6191c3da209b",
    "lastUpdated": "2026-04-21T10:49:52.1736313+00:00"
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

**Interpretation:** at least one sub-request ended in successful resource creation; combined outcome — **VULNERABLE**.

---

### 3. Unexpected Payload Injection (`UnexpectedPayloadInjectionAttack`, order 30)

**Procedure:** unknown fields, duplicate `id`, nested fragments.

**Result (32):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "id": "6656a231-bd1e-4487-9087-d5b4c1702bd0",
  "meta": {
    "versionId": "cdeee8ea-3e8c-4cb8-a45f-8048fcbcec5c",
    "lastUpdated": "2026-04-21T10:49:52.2365856+00:00"
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
{
  "resourceType": "Patient",
  "id": "f9c080a3-8344-4f12-ae5e-55215eaf0474",
  "meta": {
    "versionId": "ed42c052-d52a-46b9-b99a-0ed08feaa303",
    "lastUpdated": "2026-04-21T10:49:52.313+00:00"
  }
}
---
{
  "resourceType": "OperationOutcome",
  "id": "692be4d5-fd23-4bf3-a052-0d4385171282",
  "meta": {
    "versionId": "ba77280d-2841-45b3-9483-fd73fa14e31b",
    "lastUpdated": "2026-04-21T10:49:52.6722577+00:00"
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

**Interpretation:** the payload is handled permissively enough to yield a successful POST (possibly with partial OperationOutcome in the body — per stored `response_body` in the DB).

---

### 4. Extension Fields Misuse (`ExtensionFieldsMisuseAttack`, order 40)

**Procedure:** POST Patient with a "malicious" extension URL and `covert-payload` string.

**Result (32):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-payload"
    }
  ],
  "name": [
    {
      "family": "Test"
    }
  ],
  "id": "3e8693f6-a12b-441d-8ce0-e2d8b94a2d7d",
  "meta": {
    "versionId": "e5616df9-9f42-4126-8003-254fd3d931b5",
    "lastUpdated": "2026-04-21T10:49:53.700+00:00"
  }
}
```

**Interpretation:** the custom extension is stored with the resource — risk of covert data in extensions on an open API.

---

### 5. Contained Resource Smuggling (`ContainedResourceSmugglingAttack`, order 50)

**Procedure:** POST Patient with `contained` Binary (base64).

**Result (32):** HTTP **400**, **SECURE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "id": "64aa82aa-5c4d-4b92-843e-8f92c3f926c8",
  "meta": {
    "versionId": "13010cdb-bb4a-405e-b4cd-a8cf446084b0",
    "lastUpdated": "2026-04-21T10:49:54.0155741+00:00"
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

**Interpretation:** the only validation-style scenario on Firely that ended in rejection in this run — contained smuggling is denied.

---

### 6. Encoded Hidden Data (`EncodedHiddenDataAttack`, order 60)

**Procedure:** POST Patient with Unicode escapes in `meta.tag.display`.

**Result (32):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "meta": {
    "tag": [
      {
        "code": "x",
        "display": "Secret"
      }
    ],
    "versionId": "e18696b9-b7e9-494b-92e7-77ae9ec89e4b",
    "lastUpdated": "2026-04-21T10:49:54.206+00:00"
  },
  "id": "b073e383-9cd4-4cf2-b83c-d2379b6bf21f"
}
```

**Interpretation:** encoded tags are accepted as part of the created resource.

---

### 7. Invalid Credentials Access Test (`InvalidCredentialsAccessAttack`, order 70)

**Procedure:** OAuth token probe (if URL is found), Basic/Bearer on Patient/Observation reads.

**Result (32):** HTTP **200**, **VULNERABLE**, severity **HIGH**.

**Response body** (`test_result.response_body`):

```text
No token_endpoint discovered.
---
{
  "resourceType": "Bundle",
  "type": "searchset",
  "timestamp": "2026-04-21T10:50:03.495+00:00",
  "meta": {
    "lastUpdated": "2026-04-21T10:50:03.495+00:00",
    "versionId": "c140a35c-bc81-4009-9679-4eed1a73cf03"
  },
  "entry": [
    {
      "fullUrl": "https://server.fire.ly/r4/Patient/b073e383-9cd4-4cf2-b83c-d2379b6bf21f",
      "search": {
        "mode": "match"
      },
      "resource": {
        "resourceType": "Patient",
        "id": "b073e383-9cd4-4cf2-b83c-d2379b6bf21f",
        "meta": {
          "versionId": "e18696b9-b7e9-494b-92e7-77ae9ec89e4b",
          "lastUpdated": "2026-04-21T10:49:54.206+00:00",
          "tag": [
            {
              "code": "x",
              "display": "Secret"
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
      "url": "https://server.fire.ly/r4/?q=CfDJ8LjzO31FTPZ7oJjS9LaN61nEUdmBoxUxonYHP6q1AE8Uf-6-6Ko4-79n7zB3chkisR-XIsmfJKkBKlwae7v3tyMifM2kG1QDWVFwDYt5btWVOS9I0jkOpiAv_PjnlfWaRH0nZ3ctG1BMuht493JRQmB8R3JueNxqk1_CV0HtV3MgSD9TG7rhF4Ikz0otroiYX7mUk_NVAurHnRlWSYCGREvA2UThpUOBCqpYeYH_Eb-i"
    }
  ],
  "id": "204faa30-73e2-4a5b-b201-6026cd00393f"
}
---
Bearer probes Patient 200 | Observation 200
---
Bearer variant → HTTP 200; Bearer variant → HTTP 200; Bearer variant → HTTP 200; 
```

**Interpretation:** with advertised OAuth, reads with forged headers do not receive **401/403**.

---

### 8. Open Endpoint Detection (`OpenEndpointDetectionAttack`, order 80)

**Procedure:** compare SMART/metadata with unauthenticated GET Patient.

**Result (32):** HTTP **200**, **MISCONFIGURED**, **MEDIUM**.

**Response body** (`test_result.response_body`):

```text
well-known HTTP 501, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: {
  "resourceType": "Bundle",
  "type": "searchset",
  "timestamp": "2026-04-21T10:50:04.245+00:00",
  "meta": {
    "lastUpdated": "2026-04-21T10:50:04.245+00:00",
    "versionId": "53cc4d19-7453-4f32-9b10-074ecd173e47"
  },
  "entry": [
    {
      "fullUrl": "https://server.fire.ly/r4/Patient/b073e383-9cd4-4cf2-b83c-d2379b6bf21f",
      "search": {
        "mode": "match"
      },
      "resource": {
        "resourceType": "Patient",
        "id": "b073e383-9cd4-4cf2-b83c-d2379b6bf21f",
        "meta": {
          "versionId": "e18696b9-b7e9-494b-92e7-77ae9ec89e4b",
          "lastUpdated": "2026-04-21T10:49:54.206+00:00",
          "tag": [
            {
              "code": "x",
              "display": "Secret"
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
      "url": "https://server.fire.ly/r4/?q=CfDJ8LjzO31FTPZ7oJjS9LaN61kXm3tYkUEkVHl1SNq-Yh_lB_IXF_RU3Iq7ODaFGsSucvXkU4587_n72JZAyiViQpKHLrOlbF7i3BOytgWtQbhG5oEk7uBFjIZXNq7LtMw4i9rCuY7qmYGL9fCCpsZlay1mW8Fr-6V3ipM8CjXq0xyCY29yzRq06t2cBHBxIzNG6n3CxdOTFNvY0ov2…
```

**Interpretation:** in the Week 7 URL inventory, Firely `/.well-known/smart-configuration` returned **501**; the capability statement still indicates OAuth — anonymous Patient searchset remains available → **MISCONFIGURED**.

---

### 9. Cross-Patient Access (`CrossPatientAccessAttack`, order 90)

**Procedure:** create victim, GET Patient/{id}, Observation by subject.

**Result (32):** HTTP **200**, **VULNERABLE**, severity **HIGH**.

**Response body** (`test_result.response_body`):

```text
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

**Interpretation:** cross-patient read without a valid session in an OAuth-advertised context.

---

### 10. Unauthorized Write / ID Tampering (`UnauthorizedWriteIdTamperingAttack`, order 100)

**Procedure:** PUT Patient (name tampering), POST/GET Observation.

**Result (32):** HTTP **200**, **VULNERABLE**, **CRITICAL**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "94bea665-2420-43d3-93a9-06ae4a4dd39e",
  "name": [
    {
      "family": "TamperedFamily-b9e9ab9364d4",
      "given": [
        "Tampered-b9e9ab9364d4"
      ]
    }
  ],
  "meta": {
    "versionId": "8e945b7a-ce17-427f-b138-c5ff62458604",
    "lastUpdated": "2026-04-21T10:50:07.939+00:00"
  }
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
    "reference": "https://server.fire.ly/r4/Patient/cba8b499-abbb-49a7-ad57-ab56bbc2c1d8"
  },
  "valueString": "OwnerRefProbe-48d07033fb75",
  "id": "de4063ce-d449-4d64-9c7b-e44c2cc4025d",
  "meta": {
    "versionId": "5c573350-c084-4c9a-90c5-aa22a230643f",
    "lastUpdated": "2026-04-21T10:50:08.801+00:00"
  }
}

GET /Observation/{id}:
{
  "resourceType": "Observation",
  "id": "de4063ce-d449-4d64-9c7b-e44c2cc4025d",
  "meta": {
    "versionId": "5c573350-c084-4c9a-90c5-aa22a230643f",
    "lastUpdated": "2026-04-21T10:50:08.801+00:00"
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
    "reference": "https://server.fire.ly/r4/Patient/cba8b499-abbb-49a7-ad57-ab56bbc2c1d8"
  },
  "valueString": "OwnerRefProbe-48d07033fb75"
}
```

**Interpretation:** write and read after modification confirm critical risk for an open sandbox with an advertised security model.

---

## Overall conclusion for this server

In run **32**, Firely behaves as a **permissive R4 sandbox**: most POST scenarios yield **201 VULNERABLE**; **Contained Smuggling** is the exception (**400 SECURE**). Auth and authorization scenarios match the pattern of a public test server plus advertised OAuth → many **VULNERABLE** and **MISCONFIGURED** results on open read.
