# Security test run documentation: Azure Healthcare APIs (FHIR)

| Field | Value |
|------|----------|
| **Server** | Azure |
| **Base URL (as in URL inventory)** | `https://fhir.azurehealthcareapis.com` |
| **Database test run ID** | `test_run_id = 33` |
| **Summary metrics source** | `reports/analysis-runs-31-34-summary.tsv` |
| **Response bodies below** | As stored in PostgreSQL, column `test_result.response_body` (run 33) |

## Classification legend

| Classification | Meaning |
|---------------|--------|
| **SECURE** | Expected rejection / correct behavior. |
| **VULNERABLE** | Confirmed risk. |
| **OPEN_POLICY** | Consistent with public policy. |
| **MISCONFIGURED** | Mismatch between stated and actual policy. |
| **INCONCLUSIVE** | Cannot judge at the FHIR layer (network, DNS, TLS, firewall, missing token, etc.). |

---

## Summary table: all scenarios (run 33)

| # | Scenario | HTTP | Classification | Vulnerable (legacy) | Severity |
|---|----------|------|---------------|---------------------|-------------|
| 1 | Malformed JSON Request | 0 | INCONCLUSIVE | no | LOW |
| 2 | Metadata Manipulation | 0 | INCONCLUSIVE | no | LOW |
| 3 | Unexpected Payload Injection | 0 | INCONCLUSIVE | no | LOW |
| 4 | Extension Fields Misuse | 0 | INCONCLUSIVE | no | LOW |
| 5 | Contained Resource Smuggling | 0 | INCONCLUSIVE | no | LOW |
| 6 | Encoded Hidden Data | 0 | INCONCLUSIVE | no | LOW |
| 7 | Invalid Credentials Access Test | 0 | INCONCLUSIVE | no | LOW |
| 8 | Open Endpoint Detection | 0 | INCONCLUSIVE | no | LOW |
| 9 | Cross-Patient Access | 0 | INCONCLUSIVE | no | LOW |
| 10 | Unauthorized Write / ID Tampering | 0 | INCONCLUSIVE | no | LOW |

**Note:** HTTP **0** in the report usually means no successful HTTP response at the client layer (I/O, timeout, host unreachable, TLS failure, etc.), not a "server response with code 0".

---

## Per-scenario detail

For each scenario below, the **procedure** (as in the backend) and **run 33 outcome** are given. Interpretation is the same for every row: **the result does not reflect Azure FHIR security policy** until working connectivity exists (correct service base URL, network, OAuth2 where required — Azure Healthcare APIs typically need app registration and a token).

### 1. Malformed JSON Request (`MalformedJsonRequestAttack`, order 10)

**Procedure:** POST `/Patient` with truncated JSON and a trailing comma.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
---
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

**Interpretation:** transport/infrastructure failure; one cannot assert whether the server accepts or rejects the body.

---

### 2. Metadata Manipulation (`MetadataManipulationAttack`, order 20)

**Procedure:** three POSTs to `/Patient` with invalid meta/type/id.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
---
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
---
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

---

### 3. Unexpected Payload Injection (`UnexpectedPayloadInjectionAttack`, order 30)

**Procedure:** three POSTs with extra fields and duplicate keys.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
---
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
---
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

---

### 4. Extension Fields Misuse (`ExtensionFieldsMisuseAttack`, order 40)

**Procedure:** POST Patient with a "covert-payload" extension.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

---

### 5. Contained Resource Smuggling (`ContainedResourceSmugglingAttack`, order 50)

**Procedure:** POST Patient with contained Binary.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

---

### 6. Encoded Hidden Data (`EncodedHiddenDataAttack`, order 60)

**Procedure:** POST Patient with Unicode escapes in tag.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

---

### 7. Invalid Credentials Access Test (`InvalidCredentialsAccessAttack`, order 70)

**Procedure:** well-known + metadata + optional token endpoint; GET with invalid Basic/Bearer.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
No token_endpoint discovered.
---
Error: I/O error on GET request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
---
Bearer probes Patient 0 | Observation 0
---
Bearer variant → HTTP 0; Bearer variant → HTTP 0; Bearer variant → HTTP 0; 
```

**Interpretation:** without successful API exchange, even `token_endpoint` discovery may be incomplete; DB bodies may contain only the client error text.

---

### 8. Open Endpoint Detection (`OpenEndpointDetectionAttack`, order 80)

**Procedure:** GET well-known, metadata, unauthenticated Patient.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
well-known HTTP 0, metadata HTTP 0; GET /Patient?_count=1 → HTTP 0. Sample: Error: I/O error on GET request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

---

### 9. Cross-Patient Access (`CrossPatientAccessAttack`, order 90)

**Procedure:** create Patient and subsequent GETs without authorization.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

**Interpretation:** if Patient creation never reaches the server, the scenario never reaches the cross-patient read phase.

---

### 10. Unauthorized Write / ID Tampering (`UnauthorizedWriteIdTamperingAttack`, order 100)

**Procedure:** PUT Patient, POST Observation.

**Result (33):** HTTP **0**, **INCONCLUSIVE**.

**Response body** (`test_result.response_body`):

```text
Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com

Error: I/O error on POST request for "https://fhir.azurehealthcareapis.com/Patient": fhir.azurehealthcareapis.com
```

---

## Recommendations before re-running

1. Use the **full** Azure FHIR endpoint for your workspace (often `https://{workspace-name}.azurehealthcareapis.com` or a regional URL per Microsoft docs), not only a bare marketing hostname that does not resolve.  
2. Verify **outbound connectivity** from the machine running the backend (firewall, proxy, DNS).  
3. Writes and some reads on Azure usually require a **Bearer token**; the current public-server-oriented scenarios use anonymous/mixed probes — under strict Azure policy you may see widespread **401/403** or **INCONCLUSIVE** at setup; that is already a meaningful outcome once connectivity works.

---

## Overall conclusion for this server

Run **33** does **not** compare Azure to HAPI/Firely/SMART on FHIR security grounds: every scenario is **INCONCLUSIVE**. For a thesis or report, label this block explicitly as **"infrastructure / connectivity"** and draw security conclusions only after a successful HTTP session with a real endpoint.
