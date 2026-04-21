# Security test run documentation: HAPI Public

| Field | Value |
|------|----------|
| **Server** | HAPI Public |
| **Base URL** | `http://hapi.fhir.org/baseR4` |
| **Database test run ID** | `test_run_id = 31` |
| **Summary metrics source** | `reports/analysis-runs-31-34-summary.tsv` |
| **Response bodies below** | As stored in PostgreSQL, column `test_result.response_body` (run 31) |

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

## Summary table: all scenarios (run 31)

| # | Scenario | HTTP | Classification | Vulnerable (legacy) | Severity |
|---|----------|------|---------------|---------------------|-------------|
| 1 | Malformed JSON Request | 400 | SECURE | no | INFO |
| 2 | Metadata Manipulation | 412 | SECURE | no | INFO |
| 3 | Unexpected Payload Injection | 412 | SECURE | no | INFO |
| 4 | Extension Fields Misuse | 412 | SECURE | no | INFO |
| 5 | Contained Resource Smuggling | 412 | SECURE | no | INFO |
| 6 | Encoded Hidden Data | 412 | SECURE | no | INFO |
| 7 | Invalid Credentials Access Test | 200 | VULNERABLE | yes | HIGH |
| 8 | Open Endpoint Detection | 200 | MISCONFIGURED | no | MEDIUM |
| 9 | Cross-Patient Access | 200 | VULNERABLE | yes | HIGH |
| 10 | Unauthorized Write / ID Tampering | 200 | VULNERABLE | yes | CRITICAL |

---

## Per-scenario detail

For **each** scenario below: what the test does (backend implementation), what was observed on HAPI Public, and a short interpretation.

### 1. Malformed JSON Request (`MalformedJsonRequestAttack`, order 10)

**Procedure:** two POSTs to `{base}/Patient`: (a) truncated JSON `{ "resourceType": "Patient", `; (b) JSON with a trailing comma `{ "resourceType": "Patient", }`. Validation POST classification: success **200/201** → VULNERABLE; **400, 404, 405, 412, 422** → SECURE.

**Result (31):** HTTP **400**, **SECURE**.

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

**Interpretation:** the server rejects syntactically invalid bodies — expected behavior.

---

### 2. Metadata Manipulation (`MetadataManipulationAttack`, order 20)

**Procedure:** three POSTs to `/Patient`: wrong `meta.versionId` type (number instead of string); `resourceType: "FakeResource"`; identifier with a null byte and a suspicious `identifier`. Outcome is the worst of the three (`combineWorstAll`).

**Result (31):** HTTP **412**, **SECURE**.

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

**Interpretation:** incorrect/suspicious metadata does not lead to successful resource creation.

---

### 3. Unexpected Payload Injection (`UnexpectedPayloadInjectionAttack`, order 30)

**Procedure:** three POSTs to `/Patient`: unknown field and `__proto__`; duplicate `id` key; nested extra fields. Classification same as validation POST.

**Result (31):** HTTP **412**, **SECURE**.

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

**Interpretation:** extended/anomalous JSON is not accepted as valid Patient creation.

---

### 4. Extension Fields Misuse (`ExtensionFieldsMisuseAttack`, order 40)

**Procedure:** one POST to `/Patient` with a custom `extension` (URL `http://malicious.example/hidden`, `valueString: covert-payload`).

**Result (31):** HTTP **412**, **SECURE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/131944401</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/131944401"
  } ]
}
```

**Interpretation:** the extension payload is not persisted with a successful **201** — validation boundaries hold.

---

### 5. Contained Resource Smuggling (`ContainedResourceSmugglingAttack`, order 50)

**Procedure:** POST `/Patient` with `contained[]`: nested `Binary` with base64 `c2VjcmV0LWRhdGE=`.

**Result (31):** HTTP **412**, **SECURE**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/131944399</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/131944399"
  } ]
}
```

**Interpretation:** the nested contained resource does not pass as an allowed create operation (or is rejected by server policy).

---

### 6. Encoded Hidden Data (`EncodedHiddenDataAttack`, order 60)

**Procedure:** POST `/Patient` with `meta.tag` where `display` uses Unicode escapes (`\u0053\u0065...` → "Secret").

**Result (31):** HTTP **412**, **SECURE**.

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

**Interpretation:** JSON-encoded tags do not lead to immediate acceptance of the test Patient.

---

### 7. Invalid Credentials Access Test (`InvalidCredentialsAccessAttack`, order 70)

**Procedure:** composite scenario: (1) resolve `token_endpoint` via `/.well-known/smart-configuration` and `/metadata`; if a URL exists — POST `client_credentials` with an invalid client; (2) GET `/Patient?_count=1` with **Invalid Basic** header; (3–4) GET with forged/malformed Bearer on `/Patient` and `/Observation`; variants include empty Bearer, non-JWT, expired-shaped token. Final outcome combines the "worst" results.

**Result (31):** HTTP **200**, **VULNERABLE**, severity **HIGH**.

**Response body** (`test_result.response_body`):

```text
No token_endpoint discovered.
---
{
  "resourceType": "Bundle",
  "id": "b2eed631-c81e-4184-b9bf-0c75202d8dbe",
  "meta": {
    "lastUpdated": "2026-04-21T08:54:14.274+00:00"
  },
  "type": "searchset",
  "link": [ {
    "relation": "self",
    "url": "https://hapi.fhir.org/baseR4/Patient?_count=1"
  }, {
    "relation": "next",
    "url": "https://hapi.fhir.org/baseR4?_getpages=b2eed631-c81e-4184-b9bf-0c75202d8dbe&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
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

**Interpretation:** when OAuth/SMART is advertised in metadata, reads with invalid credentials are not blocked with **401/403** — a mismatch between advertised authentication and actual behavior.

---

### 8. Open Endpoint Detection (`OpenEndpointDetectionAttack`, order 80)

**Procedure:** GET `/.well-known/smart-configuration`, GET `/metadata` (check whether OAuth is advertised); then unauthenticated GET `/Patient?_count=1`. If OAuth is advertised but Patient can be read without authorization → **MISCONFIGURED**.

**Result (31):** HTTP **200**, **MISCONFIGURED**, **MEDIUM**.

**Response body** (`test_result.response_body`):

```text
well-known HTTP 404, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: {
  "resourceType": "Bundle",
  "id": "b2eed631-c81e-4184-b9bf-0c75202d8dbe",
  "meta": {
    "lastUpdated": "2026-04-21T08:54:14.274+00:00"
  },
  "type": "searchset",
  "link": [ {
    "relation": "self",
    "url": "https://hapi.fhir.org/baseR4/Patient?_count=1"
  }, {
    "relation": "next",
    "url": "https://hapi.fhir.org/baseR4?_getpages=b2eed631-c81e-4184-b9bf-0c75202d8dbe&_getpagesoffset=1&_count=1&_pretty=true&_bundletype=searchset"
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

**Interpretation:** mismatch between advertised security mechanisms and the ability to read Patient anonymously.

---

### 9. Cross-Patient Access (`CrossPatientAccessAttack`, order 90)

**Procedure:** a victim Patient is created, then without authorization: GET `/Patient/{victimId}` and Observation search with `subject=Patient/{victimId}`. On **200** with advertised OAuth → **VULNERABLE**; without OAuth — **OPEN_POLICY**.

**Result (31):** HTTP **200**, **VULNERABLE**, **HIGH**.

**Response body** (`test_result.response_body`):

```text
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

**Interpretation:** cross-patient read by direct id and via Observation is possible where OAuth is considered available in the environment — API-level IDOR-style risk.

---

### 10. Unauthorized Write / ID Tampering (`UnauthorizedWriteIdTamperingAttack`, order 100)

**Procedure:** (1) PUT `/Patient/{id}` tampering names on an existing resource; (2) POST Observation for subject + follow-up GET Observation. On **200/201** success with advertised OAuth → **CRITICAL VULNERABLE**.

**Result (31):** HTTP **200**, **VULNERABLE**, **CRITICAL**.

**Response body** (`test_result.response_body`):

```text
{
  "resourceType": "Patient",
  "id": "131944444",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-04-21T08:54:27.633+00:00",
    "source": "#i439fl4z6KcTcZCm"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Tampered-de30d1405e80 <b>TAMPEREDFAMILY-DE30D1405E80 </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "name": [ {
    "family": "TamperedFamily-de30d1405e80",
    "given": [ "Tampered-de30d1405e80" ]
  } ]
}

POST /Observation:
{
  "resourceType": "Observation",
  "id": "131944446",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T08:54:29.632+00:00",
    "source": "#lAyFxcH9MKsOC6dq"
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
    "reference": "Patient/131944445"
  },
  "valueString": "OwnerRefProbe-6f4071083e24"
}

GET /Observation/{id}:
{
  "resourceType": "Observation",
  "id": "131944446",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-04-21T08:54:29.632+00:00",
    "source": "#lAyFxcH9MKsOC6dq"
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
    "reference": "Patient/131944445"
  },
  "valueString": "OwnerRefProbe-6f4071083e24"
}
```

**Interpretation:** anonymous or insufficiently authorized data modification and related records are confirmed — highest priority among write scenarios.

---

## Overall conclusion for this server

HAPI Public shows **hard rejection** (**412** / **400**) on most validation and covert-channel POST checks — a **positive signal** for the parsing/rules layer. At the same time, **authentication, open read with advertised OAuth, cross-patient read, and write** scenarios are marked **VULNERABLE** or **MISCONFIGURED**, reflecting public demo policy rather than production configuration with strict RBAC/ABAC.
