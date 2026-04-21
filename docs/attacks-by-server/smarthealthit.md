# Security test run documentation: SMART Health IT (R4)

| Field | Value |
|------|----------|
| **Server** | Smarthealthit |
| **Base URL** | `https://r4.smarthealthit.org` |
| **Database test run ID** | `test_run_id = 34` |
| **Summary metrics source** | `reports/analysis-runs-31-34-summary.tsv` |
| **Response bodies below** | As stored in PostgreSQL, column `test_result.response_body` (run 34) |

## Classification legend

| Classification | Meaning |
|---------------|--------|
| **SECURE** | Expected rejection / correct behavior. |
| **VULNERABLE** | Confirmed risk. |
| **OPEN_POLICY** | Consistent with public policy (no advertised OAuth). |
| **MISCONFIGURED** | OAuth/SMART is advertised, but anonymous access is inconsistent. |
| **INCONCLUSIVE** | No conclusion possible. |

---

## Summary table: all scenarios (run 34)

| # | Scenario | HTTP | Classification | Vulnerable (legacy) | Severity |
|---|----------|------|---------------|---------------------|-------------|
| 1 | Malformed JSON Request | 400 | SECURE | no | INFO |
| 2 | Metadata Manipulation | 201 | VULNERABLE | yes | HIGH |
| 3 | Unexpected Payload Injection | 201 | VULNERABLE | yes | HIGH |
| 4 | Extension Fields Misuse | 201 | VULNERABLE | yes | HIGH |
| 5 | Contained Resource Smuggling | 201 | VULNERABLE | yes | HIGH |
| 6 | Encoded Hidden Data | 201 | VULNERABLE | yes | HIGH |
| 7 | Invalid Credentials Access Test | 200 | VULNERABLE | yes | HIGH |
| 8 | Open Endpoint Detection | 200 | MISCONFIGURED | no | MEDIUM |
| 9 | Cross-Patient Access | 200 | VULNERABLE | yes | HIGH |
| 10 | Unauthorized Write / ID Tampering | 200 | VULNERABLE | yes | CRITICAL |

---

## Per-scenario detail

### 1. Malformed JSON Request (`MalformedJsonRequestAttack`, order 10)

**Procedure:** POST `/Patient` — truncated JSON and trailing comma.

**Result (34):** HTTP **400**, **SECURE**.

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

**Interpretation:** as on HAPI, syntactically invalid JSON is rejected; unlike Firely, there is no **201** on this scenario.

---

### 2. Metadata Manipulation (`MetadataManipulationAttack`, order 20)

**Procedure:** three POSTs: numeric `versionId`, `FakeResource`, id with null byte.

**Result (34):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "3677941",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:49.504-04:00"
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
  "id": "3677942",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:49.817-04:00"
  },
  "identifier": [
    {
      "system": "urn:test",
      "value": "id;secret=1"
    }
  ]
}
```

**Interpretation:** combined outcome — successful Patient creation on one or more steps; stored `response_body` may concatenate several responses (including OperationOutcome on wrong resource type and subsequent successful Patient responses).

---

### 3. Unexpected Payload Injection (`UnexpectedPayloadInjectionAttack`, order 30)

**Procedure:** unknown fields, duplicate `id`, nested structures.

**Result (34):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "3677943",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:49.958-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}
---
{
  "resourceType": "Patient",
  "id": "3677944",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:50.097-04:00"
  }
}
---
{
  "resourceType": "Patient",
  "id": "3677945",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:50.238-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}
```

**Interpretation:** permissive handling of extended JSON with **201** outcome.

---

### 4. Extension Fields Misuse (`ExtensionFieldsMisuseAttack`, order 40)

**Procedure:** extension with a "secret" string.

**Result (34):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "3677946",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:50.378-04:00"
  },
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
  ]
}
```

**Interpretation:** the extension is stored on the created resource (as in a typical FHIR store).

---

### 5. Contained Resource Smuggling (`ContainedResourceSmugglingAttack`, order 50)

**Procedure:** Patient + contained Binary (base64).

**Result (34):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "3677947",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:50.520-04:00"
  }
}
```

**Interpretation:** unlike Firely (**400 SECURE**) and HAPI (**412 SECURE**), the SMART sandbox accepted the smuggling payload in this run.

---

### 6. Encoded Hidden Data (`EncodedHiddenDataAttack`, order 60)

**Procedure:** Unicode escapes in `meta.tag.display`.

**Result (34):** HTTP **201**, **VULNERABLE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "3677948",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:50.659-04:00",
    "tag": [
      {
        "code": "x",
        "display": "Secret"
      }
    ]
  }
}
```

**Interpretation:** tags are decoded and appear on the stored resource (including display "Secret").

---

### 7. Invalid Credentials Access Test (`InvalidCredentialsAccessAttack`, order 70)

**Procedure:** OAuth/token when URL is discovered; Basic/Bearer on read.

**Result (34):** HTTP **200**, **VULNERABLE**, severity **HIGH**.

**Response body** (`test_result.response_body`):

```text
No token_endpoint discovered.
---
{
  "resourceType": "Bundle",
  "id": "1701b4b3-f46b-4fc7-84a9-507fdd6b5fba",
  "meta": {
    "lastUpdated": "2026-04-21T06:54:52.062-04:00"
  },
  "type": "searchset",
  "link": [
    {
      "relation": "self",
      "url": "https://r4.smarthealthit.org/Patient?_count=1"
    },
    {
      "relation": "next",
      "url": "https://r4.smarthealthit.org?_getpages=1701b4b3-f46b-4fc7-84a9-507fdd6b5fba&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
    }
  ],
  "entry": [
    {
      "fullUrl": "https://r4.smarthealthit.org/Patient/a74651a6-8141-4c7e-91b5-a43ce80e6b92",
      "resource": {
        "resourceType": "Patient",
        "id": "a74651a6-8141-4c7e-91b5-a43ce80e6b92",
        "meta": {
          "versionId": "36",
          "lastUpdated": "2026-02-07T02:19:27.999-05:00",
          "tag": [
            {
              "system": "https://smarthealthit.org/tags",
              "code": "synthea-5-2019"
            }
          ]
        },
        "text": {
          "status": "generated",
          "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\">Generated by <a href=\"https://github.com/synthetichealth/synthea\">Synthea</a>.Version identifier: v2.4.0-100-g26a4b936\n .   Person seed: -2671994955959312395  Population seed: 1559319163074</div>"
        },
        "extension": [
          {
            "url": "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race",
            "extension": [
              {
                "url": "ombCategory",
      …
---
Bearer probes Patient 200 | Observation 200
---
Bearer variant → HTTP 200; Bearer variant → HTTP 200; Bearer variant → HTTP 200; 
```

**Interpretation:** reads with forged credentials are not blocked in an OAuth-advertised environment.

---

### 8. Open Endpoint Detection (`OpenEndpointDetectionAttack`, order 80)

**Procedure:** well-known + metadata + unauthenticated Patient.

**Result (34):** HTTP **200**, **MISCONFIGURED**, **MEDIUM**.

**Response body** (`test_result.response_body`):

```text
well-known HTTP 404, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: {
  "resourceType": "Bundle",
  "id": "1701b4b3-f46b-4fc7-84a9-507fdd6b5fba",
  "meta": {
    "lastUpdated": "2026-04-21T06:54:52.062-04:00"
  },
  "type": "searchset",
  "link": [
    {
      "relation": "self",
      "url": "https://r4.smarthealthit.org/Patient?_count=1"
    },
    {
      "relation": "next",
      "url": "https://r4.smarthealthit.org?_getpages=1701b4b3-f46b-4fc7-84a9-507fdd6b5fba&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
    }
  ],
  "entry": [
    {
      "fullUrl": "https://r4.smarthealthit.org/Patient/a74651a6-8141-4c7e-91b5-a43ce80e6b92",
      "resource": {
        "resourceType": "Patient",
        "id": "a74651a6-8141-4c7e-91b5-a43ce80e6b92",
        "meta": {
          "versionId": "36",
          "lastUpdated": "2026-02-07T02:19:27.999-05:00",
          "tag": [
            {
              "system": "https://smarthealthit.org/tags",
              "code": "synthea-5-2019"
            }
          ]
        },
        "text": {
          "status": "generated",
          "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\">Generated by <a href=\"https://github.com/synthetichealth/synthea\">Synthea</a>.Version identifier: v2.4.0-100-g26a4…
```

**Interpretation:** for r4.smarthealthit.org, well-known may return **404** (path not exposed), while the CapabilityStatement references security/OAuth — anonymous Patient searchset yields **MISCONFIGURED**.

---

### 9. Cross-Patient Access (`CrossPatientAccessAttack`, order 90)

**Procedure:** GET another patient's Patient and Observation by subject.

**Result (34):** HTTP **200**, **VULNERABLE**, severity **HIGH**.

**Response body** (`test_result.response_body`):

```text
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

**Interpretation:** cross-patient access without valid authorization in an OAuth context.

---

### 10. Unauthorized Write / ID Tampering (`UnauthorizedWriteIdTamperingAttack`, order 100)

**Procedure:** PUT Patient (tampering), POST/GET Observation.

**Result (34):** HTTP **200**, **VULNERABLE**, **CRITICAL**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "3677952",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-04-21T06:54:55.429-04:00"
  },
  "name": [
    {
      "family": "TamperedFamily-b81859ff937c",
      "given": [
        "Tampered-b81859ff937c"
      ]
    }
  ]
}

POST /Observation:
{
  "resourceType": "Observation",
  "id": "3677954",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:56.068-04:00"
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
    "reference": "Patient/3677953"
  },
  "valueString": "OwnerRefProbe-08f72321a204"
}

GET /Observation/{id}:
{
  "resourceType": "Observation",
  "id": "3677954",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T06:54:56.068-04:00"
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
    "reference": "Patient/3677953"
  },
  "valueString": "OwnerRefProbe-08f72321a204"
}
```

**Interpretation:** modification and related write operations are confirmed.

---

## Overall conclusion for this server

**SMART Health IT R4** in run **34** is close to an **open sandbox**: almost all POST scenarios (except **Malformed JSON**) → **201 VULNERABLE**; **Malformed JSON** stays **400 SECURE**. **MISCONFIGURED** open-endpoint plus **CRITICAL** write reflects a training/test platform, not a reference for strict patient isolation without tokens.
