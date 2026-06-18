# Attack run — Firely

- **Base URL:** `https://server.fire.ly/r4`
- **Test run ID:** 13
- **Started at:** 2026-05-26T13:44:23.179037
- **Scenarios:** 12
- **VULNERABLE count:** 3
- **Non-NONE leakage:** 0

## Summary table

| # | Scenario | Status | Classification | Severity | Leakage | Vulnerable |
|---|----------|--------|----------------|----------|---------|------------|
| 1 | Malformed JSON Request | 201 | `VULNERABLE` | HIGH | `NONE` | True |
| 2 | Metadata Manipulation | 201 | `SECURE` | INFO | `NONE` | False |
| 3 | Unexpected Payload Injection | 400 | `SECURE` | INFO | `NONE` | False |
| 4 | Extension Fields Misuse | 201 | `VULNERABLE` | MEDIUM | `NONE` | True |
| 5 | Contained Resource Smuggling | 400 | `SECURE` | INFO | `NONE` | False |
| 6 | Encoded Hidden Data | 201 | `VULNERABLE` | LOW | `NONE` | True |
| 7 | Invalid Credentials Access Test | 200 | `INCONCLUSIVE` | LOW | `NONE` | False |
| 8 | Open Endpoint Detection | 200 | `OPEN_POLICY` | INFO | `NONE` | False |
| 9 | Authenticated Token Isolation | 0 | `INCONCLUSIVE` | LOW | `NONE` | False |
| 10 | Cross-Patient Access | 200 | `OPEN_POLICY` | INFO | `NONE` | False |
| 11 | Observation Bundle / Duplicate Clinical | 400 | `SECURE` | INFO | `NONE` | False |
| 12 | Unauthorized Write / ID Tampering | 200 | `OPEN_POLICY` | INFO | `NONE` | False |

## Detailed results

### Malformed JSON Request

| Field | Value |
|-------|-------|
| **testResultId** | 133 |
| **HTTP status** | 201 |
| **Classification** | `VULNERABLE` |
| **Vulnerable** | True |
| **Severity** | HIGH |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,json.syntax_invalid,json.truncation` |

**Reason:**

Trailing comma JSON: malformed JSON resulted in a retrievable Patient resource (partial parsing / permissive parser). | Also: SECURE — Truncated JSON: server rejected malformed JSON as expected.

**Request log:**

```http
POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{ "resourceType": "Patient",

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{ "resourceType": "Patient", }

---

GET https://server.fire.ly/r4/Patient/6ac720d3-8d01-4ec6-9075-2e2094725e13
```

**Response body:**

```
{
  "resourceType": "OperationOutcome",
  "id": "182bdf7e-140c-439e-ad0e-6897afb55dde",
  "meta": {
    "versionId": "e04a2790-4f28-4f33-a1b4-9a3352c6c6f4",
    "lastUpdated": "2026-05-26T11:44:23.4251375+00:00"
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
  "id": "6ac720d3-8d01-4ec6-9075-2e2094725e13",
  "meta": {
    "versionId": "c52f0bd3-57ca-4879-a125-b9cabc65ba60",
    "lastUpdated": "2026-05-26T11:44:24.046+00:00"
  }
}

Follow-up GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "6ac720d3-8d01-4ec6-9075-2e2094725e13",
  "meta": {
    "versionId": "c52f0bd3-57ca-4879-a125-b9cabc65ba60",
    "lastUpdated": "2026-05-26T11:44:24.046+00:00"
  }
}
```

---
### Metadata Manipulation

| Field | Value |
|-------|-------|
| **testResultId** | 134 |
| **HTTP status** | 201 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.meta_semantics,fhir.resourceType,fhir.id_identifier` |

**Reason:**

Server did not persist/echo the invalid meta.versionId (client meta appears ignored/sanitized). | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server rejected suspicious client-supplied id as expected.

**Request log:**

```http
POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","meta":{"versionId":123}}

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"FakeResource","id":"1"}

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","id":"visible\u0000hidden-data"}
```

**Response body:**

```
{
  "resourceType": "Patient",
  "meta": {
    "versionId": "eb49e9dc-fbd2-4126-97c8-5b120d987ad9",
    "lastUpdated": "2026-05-26T11:44:24.352+00:00"
  },
  "id": "64b41dd8-d66f-4062-a323-2b07f8407aa1"
}
---
{
  "resourceType": "OperationOutcome",
  "id": "5a84b6f9-d556-44b4-b734-8f7055fd3548",
  "meta": {
    "versionId": "951d1013-4f2f-4bca-bd0f-dd3d13182936",
    "lastUpdated": "2026-05-26T11:44:24.4643477+00:00"
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
  "id": "89d86d61-7418-497f-8164-d4c321bae006",
  "meta": {
    "versionId": "bdfe7fda-8820-4b4a-a1d0-08d2b6272843",
    "lastUpdated": "2026-05-26T11:44:24.7894922+00:00"
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

---
### Unexpected Payload Injection

| Field | Value |
|-------|-------|
| **testResultId** | 135 |
| **HTTP status** | 400 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,json.unknown_fields,json.duplicate_keys,json.extra_nested` |

**Reason:**

unknownField/__proto__ injection: server rejected the payload. | Also: SECURE — Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed. | Also: SECURE — _payload/extraNested injection: server rejected the payload.

**Request log:**

```http
POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Test"}],"unknownField":"inj-unknown-6ec1383e24","__proto__":{"polluted":"inj-unknown-6ec1383e24"}}

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","id":"valid-id","id":"duplicate-id"}

---

GET https://server.fire.ly/r4/Patient/a68c211e-7b2a-476f-aaf9-5efa8f622cef

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Test"}],"_payload":"inj-nested-bd11c5571b","extraNested":{"secret":"inj-nested-bd11c5571b"}}
```

**Response body:**

```
{
  "resourceType": "OperationOutcome",
  "id": "7239a688-ff7b-40c6-8a08-d82caf9c3983",
  "meta": {
    "versionId": "8599b6b4-429b-4bf6-93a2-89b613c24af9",
    "lastUpdated": "2026-05-26T11:44:24.8369855+00:00"
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
  "id": "a68c211e-7b2a-476f-aaf9-5efa8f622cef",
  "meta": {
    "versionId": "bc437367-cdfb-4e60-9985-dcb59c811e24",
    "lastUpdated": "2026-05-26T11:44:24.921+00:00"
  }
}

Follow-up GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "a68c211e-7b2a-476f-aaf9-5efa8f622cef",
  "meta": {
    "versionId": "bc437367-cdfb-4e60-9985-dcb59c811e24",
    "lastUpdated": "2026-05-26T11:44:24.921+00:00"
  }
}
---
{
  "resourceType": "OperationOutcome",
  "id": "777b506c-b46d-4389-883c-94f304b6a6b4",
  "meta": {
    "versionId": "0208d499-1620-4db0-8b43-d5c7ef82db54",
    "lastUpdated": "2026-05-26T11:44:25.1626691+00:00"
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

---
### Extension Fields Misuse

| Field | Value |
|-------|-------|
| **testResultId** | 136 |
| **HTTP status** | 201 |
| **Classification** | `VULNERABLE` |
| **Vulnerable** | True |
| **Severity** | MEDIUM |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.extension.covert_channel,fhir.extension.valueString` |

**Reason:**

Custom extension marker persisted and is retrievable (potential covert storage channel).

**Request log:**

```http
POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Probe-covert-ext-d6768de3078b"}],"extension":[{"url":"http://malicious.example/hidden","valueString":"covert-ext-d6768de3078b"}]}

---

GET https://server.fire.ly/r4/Patient/3c5619bc-3a6f-4410-8991-48673241d1bf
```

**Response body:**

```
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-ext-d6768de3078b"
    }
  ],
  "name": [
    {
      "family": "Probe-covert-ext-d6768de3078b"
    }
  ],
  "id": "3c5619bc-3a6f-4410-8991-48673241d1bf",
  "meta": {
    "versionId": "9c511775-7f03-4285-a0c5-6bde8dcca84f",
    "lastUpdated": "2026-05-26T11:44:26.394+00:00"
  }
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "3c5619bc-3a6f-4410-8991-48673241d1bf",
  "meta": {
    "versionId": "9c511775-7f03-4285-a0c5-6bde8dcca84f",
    "lastUpdated": "2026-05-26T11:44:26.394+00:00"
  },
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-ext-d6768de3078b"
    }
  ],
  "name": [
    {
      "family": "Probe-covert-ext-d6768de3078b"
    }
  ]
}
```

---
### Contained Resource Smuggling

| Field | Value |
|-------|-------|
| **testResultId** | 137 |
| **HTTP status** | 400 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.contained.binary,fhir.base64_payload` |

**Reason:**

Server rejected the payload (no contained-resource persistence).

**Request log:**

```http
POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Probe-covert-bin-6aa856f82486"}],"contained":[{"resourceType":"Binary","id":"covert","contentType":"text/plain","data":"Y292ZXJ0LWJpbi02YWE4NTZmODI0ODY="}]}
```

**Response body:**

```
{
  "resourceType": "OperationOutcome",
  "id": "f47f5494-f769-4e91-a380-79f1a6d475ad",
  "meta": {
    "versionId": "980f6ec2-940f-4fcb-aca9-4464fdf91ad9",
    "lastUpdated": "2026-05-26T11:44:26.6533089+00:00"
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

---
### Encoded Hidden Data

| Field | Value |
|-------|-------|
| **testResultId** | 138 |
| **HTTP status** | 201 |
| **Classification** | `VULNERABLE` |
| **Vulnerable** | True |
| **Severity** | LOW |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.meta.tag,unicode.normalization_display` |

**Reason:**

Meta.tag display marker persisted and is retrievable (potential encoded covert storage channel).

**Request log:**

```http
POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","meta":{"tag":[{"code":"x","display":"\u0063\u006f\u0076\u0065\u0072\u0074\u002d\u0074\u0061\u0067\u002d\u0038\u0034\u0061\u0039\u0062\u0031\u0064\u0064\u0031\u0036\u0066\u0062"}]}}

---

GET https://server.fire.ly/r4/Patient/2f9ac0f9-caca-47f7-847e-d72798e16b02
```

**Response body:**

```
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "meta": {
    "tag": [
      {
        "code": "x",
        "display": "covert-tag-84a9b1dd16fb"
      }
    ],
    "versionId": "0a6448bf-c928-43f9-8d75-82f47cb4c815",
    "lastUpdated": "2026-05-26T11:44:26.783+00:00"
  },
  "id": "2f9ac0f9-caca-47f7-847e-d72798e16b02"
}

GET /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "2f9ac0f9-caca-47f7-847e-d72798e16b02",
  "meta": {
    "versionId": "0a6448bf-c928-43f9-8d75-82f47cb4c815",
    "lastUpdated": "2026-05-26T11:44:26.783+00:00",
    "tag": [
      {
        "code": "x",
        "display": "covert-tag-84a9b1dd16fb"
      }
    ]
  }
}
```

---
### Invalid Credentials Access Test

| Field | Value |
|-------|-------|
| **testResultId** | 139 |
| **HTTP status** | 200 |
| **Classification** | `INCONCLUSIVE` |
| **Vulnerable** | False |
| **Severity** | LOW |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.get.patient,http.get.observation,auth.basic_invalid,auth.bearer_forged,auth.oauth_token_endpoint` |

**Reason:**

OAuth token URL not available; sub-probe skipped. | Also: OPEN_POLICY — Read succeeded with bad credentials ignored — typical for public demo servers without advertised OAuth. | Also: OPEN_POLICY — Read succeeded with bad credentials ignored — typical for public demo servers without advertised OAuth. | Also: OPEN_POLICY — Read succeeded with bad credentials ignored — typical for public demo servers without advertised OAuth. | Also: OPEN_POLICY — Read succeeded with bad credentials ignored — typical for public demo servers without advertised OAuth. | Also: OPEN_POLICY — Read succeeded with bad credentials ignored — typical for public demo servers without advertised OAuth. | Also: OPEN_POLICY — Read succeeded with bad credentials ignored — typical for public demo servers without advertised OAuth.

**Request log:**

```http
GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata

---

GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata

---

GET https://server.fire.ly/r4/Patient?_count=1
Authorization: Basic aW52YWxpZDppbnZhbGlk

---

GET https://server.fire.ly/r4/Patient?_count=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid_signature_week7

---

GET https://server.fire.ly/r4/Observation?_count=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid_signature_week7

---

GET https://server.fire.ly/r4/Patient?_count=1
Authorization: Bearer

---

GET https://server.fire.ly/r4/Patient?_count=1
Authorization: Bearer not.a.valid.jwt.week7

---

GET https://server.fire.ly/r4/Patient?_count=1
Authorization: Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJleHAiOjE1MDAwMDAwMDAsInN1YiI6IndlZWs3In0.x
```

**Response body:**

```
No token_endpoint discovered.
---
{
  "resourceType": "Bundle",
  "type": "searchset",
  "timestamp": "2026-05-26T11:44:36.744+00:00",
  "meta": {
    "lastUpdated": "2026-05-26T11:44:36.744+00:00",
    "versionId": "ef9bbfe2-b33c-4f2f-8aaf-4402ecc4c3bf"
  },
  "entry": [
    {
      "fullUrl": "https://server.fire.ly/r4/Patient/2f9ac0f9-caca-47f7-847e-d72798e16b02",
      "search": {
        "mode": "match"
      },
      "resource": {
        "resourceType": "Patient",
        "id": "2f9ac0f9-caca-47f7-847e-d72798e16b02",
        "meta": {
          "versionId": "0a6448bf-c928-43f9-8d75-82f47cb4c815",
          "lastUpdated": "2026-05-26T11:44:26.783+00:00",
          "tag": [
            {
              "code": "x",
              "display": "covert-tag-84a9b1dd16fb"
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
      "url": "https://server.fire.ly/r4/?q=CfDJ8LjzO31FTPZ7oJjS9LaN61kZLpYErdRT8EPTXOnmQV4cmDny8ipHSVOwX3zFmT9RvOcF3XhPX4fAeHmWHQe--FN3GW8dPW8fHHyw8uW0nrsx_WMnt7cOQLEiFrjeCGIxlyuzyVmjZ21hyay6Z_sbuKY-P3SQSOKAMqFiDr_bc2ZfTzz6uMOEDDwn0Zpg_BLHXFM_p6JgnRbxFy4v179cdfNy3pgsajV1a7-I96IqQKL0"
    }
  ],
  "id": "138f561d-e76c-4294-9670-c9dd9a0d134a"
}
---
Bearer probes Patient 200 | Observation 200
---
Bearer variant → HTTP 200; Bearer variant → HTTP 200; Bearer variant → HTTP 200;
```

---
### Open Endpoint Detection

| Field | Value |
|-------|-------|
| **testResultId** | 140 |
| **HTTP status** | 200 |
| **Classification** | `OPEN_POLICY` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.get.metadata,http.get.smart_well_known,http.get.patient_anonymous` |

**Reason:**

No OAuth/SMART in metadata; anonymous Patient read matches typical public demo policy.

**Request log:**

```http
GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata

---

GET https://server.fire.ly/r4/Patient?_count=1
```

**Response body:**

```
well-known HTTP 501, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: {
  "resourceType": "Bundle",
  "type": "searchset",
  "timestamp": "2026-05-26T11:44:37.465+00:00",
  "meta": {
    "lastUpdated": "2026-05-26T11:44:37.465+00:00",
    "versionId": "94b60cfb-e73f-41d3-9e73-e0d7e97276ce"
  },
  "entry": [
    {
      "fullUrl": "https://server.fire.ly/r4/Patient/2f9ac0f9-caca-47f7-847e-d72798e16b02",
      "search": {
        "mode": "match"
      },
      "resource": {
        "resourceType": "Patient",
        "id": "2f9ac0f9-caca-47f7-847e-d72798e16b02",
        "meta": {
          "versionId": "0a6448bf-c928-43f9-8d75-82f47cb4c815",
          "lastUpdated": "2026-05-26T11:44:26.783+00:00",
          "tag": [
            {
              "code": "x",
              "display": "covert-tag-84a9b1dd16fb"
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
      "url": "https://server.fire.ly/r4/?q=CfDJ8LjzO31FTPZ7oJjS9LaN61mLH1hpbXoVnR5VEmG_JUan2NBSXSmaeyJe_9x1YuQAbRH1liUvMWIvLOSdKAnowW9eTswZ5uCyVvOYm0hg_LTGDATh8ACScYYugrlHao4XBou1OJQkQZy3ve0jYc86t31C2hPnW_jzmzENcoabLkeb-15xdxg-5jq_DKawhUl…
```

---
### Authenticated Token Isolation

| Field | Value |
|-------|-------|
| **testResultId** | 141 |
| **HTTP status** | 0 |
| **Classification** | `INCONCLUSIVE` |
| **Vulnerable** | False |
| **Severity** | LOW |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `auth.bearer_lab_token,http.get.patient_out_of_scope,auth.token_isolation` |

**Reason:**

N/A: No bearer token configured (set FHIR_SECURITY_TEST_BEARER_TOKEN or fhir.security.test.bearer-token); authenticated isolation probe skipped.

**Request log:**

```http
GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata
```

**Response body:**

_empty_

---
### Cross-Patient Access

| Field | Value |
|-------|-------|
| **testResultId** | 142 |
| **HTTP status** | 200 |
| **Classification** | `OPEN_POLICY` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.get.patient_by_id,http.post.observation_subject,http.get.observation_search,idor.cross_patient` |

**Reason:**

Cross-patient read succeeded — typical for fully open public FHIR demos (no OAuth in metadata). | Also: OPEN_POLICY — Cross-patient read succeeded — typical for fully open public FHIR demos (no OAuth in metadata).

**Request log:**

```http
GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Attacker-6349c92fbd6d"],"family":"PatientA-6349c92fbd6d"}]}

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-b731948b1c50"],"family":"PatientB-b731948b1c50"}]}

---

GET https://server.fire.ly/r4/Patient/0e0d2c47-7a4d-41a5-af48-65e317c24a81

---

POST https://server.fire.ly/r4/Observation
Content-Type: application/json

{"resourceType":"Observation","status":"final","code":{"text":"SecurityTest","coding":[{"system":"http://loinc.org","code":"718-7"}]},"subject":{"reference":"Patient/0e0d2c47-7a4d-41a5-af48-65e317c24a81"},"valueString":"CrossPatientProbe-7d0cf95c8f8b"}

---

GET https://server.fire.ly/r4/Observation?subject=Patient%2F0e0d2c47-7a4d-41a5-af48-65e317c24a81
```

**Response body:**

```
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

---
### Observation Bundle / Duplicate Clinical

| Field | Value |
|-------|-------|
| **testResultId** | 143 |
| **HTTP status** | 400 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `fhir.bundle.transaction,http.post.observation_batch,fhir.observation.duplicate_clinical` |

**Reason:**

Server rejected the duplicate-clinical bundle (expected integrity / validation behavior).

**Request log:**

```http
GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["BundleDup-da4ccc4edab1"],"family":"ClinicalSubject-da4ccc4edab1"}]}

---

POST https://server.fire.ly/r4
Content-Type: application/json

{"resourceType":"Bundle","type":"transaction","entry":[{"fullUrl":"urn:uuid:week11-dup-0-e7158bb8dc12","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/ceed9739-d7cc-4e26-a365-78a56bf126df"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-e7158bb8dc12"}]},"request":{"method":"POST","url":"Observation"}},{"fullUrl":"urn:uuid:week11-dup-1-e7158bb8dc12","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/ceed9739-d7cc-4e26-a365-78a56bf126df"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-e7158bb8dc12"}]},"request":{"method":"POST","url":"Observation"}},{"fullUrl":"urn:uuid:week11-dup-2-e7158bb8dc12","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/ceed9739-d7cc-4e26-a365-78a56bf126df"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-e7158bb8dc12"}]},"request":{"method":"POST","url":"Observation"}}]}
```

**Response body:**

```
POST transaction Bundle (3 duplicate-structure Observations for Patient/ceed9739-d7cc-4e26-a365-78a56bf126df) → HTTP 400. Created-like entry responses: 0. {
  "resourceType": "OperationOutcome",
  "id": "6b1edf6f-74a3-4397-914e-59d963126595",
  "meta": {
    "versionId": "640b8b4b-d93a-49ee-b9be-12fb9b2f59d5",
    "lastUpdated": "2026-05-26T11:44:40.6346544+00:00"
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
          "valueInteger": 103
        },
        {
          "url": "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-source",
          "valueString": "FhirUriValidator"
        }
      ],
      "severity": "error",
      "code": "invalid",
      "details": {
        "coding": [
          {
            "system": "http://hl7.org/fhir/dotnet-api-operation-outcome",
            "code": "1006"
          }
        ],
        "text": "Value 'urn:uuid:week11-dup-0-e7158bb8dc12' is not a valid URI"
      },
      "diagnostics": "ElementDefinition trace: Bundle.entry.fullUrl->uri",
      "expression": [
        "Bundle.entry[0].fullUrl[0]"
      ]
    }
  ]
}
```

---
### Unauthorized Write / ID Tampering

| Field | Value |
|-------|-------|
| **testResultId** | 144 |
| **HTTP status** | 200 |
| **Classification** | `OPEN_POLICY` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.put.patient,http.post.observation,http.get.observation,auth.anonymous_write` |

**Reason:**

Anonymous write succeeded — common on open sandboxes for testing (no OAuth advertised). Follow-up GET shows tampered marker persisted. | Also: OPEN_POLICY — Anonymous write succeeded — common on open sandboxes for testing (no OAuth advertised). Follow-up GET HTTP 200.

**Request log:**

```http
GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-26eb9826dada"],"family":"PatientVictim-26eb9826dada"}]}

---

GET https://server.fire.ly/r4/Patient/38e37a31-df6d-4d3b-a843-c6b6ed1500d0

---

PUT https://server.fire.ly/r4/Patient/38e37a31-df6d-4d3b-a843-c6b6ed1500d0
Content-Type: application/json

{"resourceType":"Patient","id":"38e37a31-df6d-4d3b-a843-c6b6ed1500d0","name":[{"given":["Tampered-d4eef0d9a773"],"family":"TamperedFamily-d4eef0d9a773"}]}

---

GET https://server.fire.ly/r4/Patient/38e37a31-df6d-4d3b-a843-c6b6ed1500d0

---

GET https://server.fire.ly/r4/.well-known/smart-configuration

---

GET https://server.fire.ly/r4/metadata

---

POST https://server.fire.ly/r4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-6d1dee2cc218"],"family":"PatientOwnerRef-6d1dee2cc218"}]}

---

POST https://server.fire.ly/r4/Observation
Content-Type: application/json

{"resourceType":"Observation","status":"final","code":{"text":"SecurityTest","coding":[{"system":"http://loinc.org","code":"718-7"}]},"subject":{"reference":"Patient/52661e0f-b8fa-4c48-936e-e936fc8fad38"},"valueString":"OwnerRefProbe-68447c708544"}

---

GET https://server.fire.ly/r4/Observation/f7d87738-4f9d-4c0c-a2ac-0408eef4dc8e
```

**Response body:**

```
GET /Patient/{id} before PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "38e37a31-df6d-4d3b-a843-c6b6ed1500d0",
  "meta": {
    "versionId": "82c2aa00-8897-49e2-92e0-37da6d79b48c",
    "lastUpdated": "2026-05-26T11:44:41.312+00:00"
  },
  "name": [
    {
      "family": "PatientVictim-26eb9826dada",
      "given": [
        "Victim-26eb9826dada"
      ]
    }
  ]
}

PUT /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "38e37a31-df6d-4d3b-a843-c6b6ed1500d0",
  "name": [
    {
      "family": "TamperedFamily-d4eef0d9a773",
      "given": [
        "Tampered-d4eef0d9a773"
      ]
    }
  ],
  "meta": {
    "versionId": "dab1bddb-dcf9-428a-8d03-2d29ab1841fa",
    "lastUpdated": "2026-05-26T11:44:41.853+00:00"
  }
}

GET /Patient/{id} after PUT (HTTP 200):
{
  "resourceType": "Patient",
  "id": "38e37a31-df6d-4d3b-a843-c6b6ed1500d0",
  "meta": {
    "versionId": "dab1bddb-dcf9-428a-8d03-2d29ab1841fa",
    "lastUpdated": "2026-05-26T11:44:41.853+00:00"
  },
  "name": [
    {
      "family": "TamperedFamily-d4eef0d9a773",
      "given": [
        "Tampered-d4eef0d9a773"
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
    "reference": "https://server.fire.ly/r4/Patient/52661e0f-b8fa-4c48-936e-e936fc8fad38"
  },
  "valueString": "OwnerRefProbe-68447c708544",
  "id": "f7d87738-4f9d-4c0c-a2ac-0408eef4dc8e",
  "meta": {
    "versionId": "5e1ca1a5-e1ea-429f-b750-40b62f79380e",
    "lastUpdated": "2026-05-26T11:44:42.673+00:00"
  }
}

GET /Observation/{id}:
{
  "resourceType": "Observation",
  "id": "f7d87738-4f9d-4c0c-a2ac-0408eef4dc8e",
  "meta": {
    "versionId": "5e1ca1a5-e1ea-429f-b750-40b62f79380e",
    "lastUpdated": "2026-05-26T11:44:42.673+00:00"
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
    "reference": "https://server.fire.ly/r4/Patient/52661e0f-b8fa-4c48-936e-e936fc8fad38"
  },
  "valueString": "OwnerRefProbe-68447c708544"
}
```

---
