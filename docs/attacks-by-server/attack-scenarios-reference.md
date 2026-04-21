# Attack scenarios reference

This document describes **each security probe** implemented in the backend: what it tries to prove, and **which HTTP requests and bodies** are sent. All paths are relative to the configured FHIR **base URL** (trailing slash normalized).

Implementation package: `com.fhir.security.attack`  
Execution order follows `@Order(10)` … `@Order(100)` (stable scenario ordering in the runner).

---

## 1. Malformed JSON Request

| | |
|---|---|
| **Display name** | `Malformed JSON Request` |
| **Java class** | `MalformedJsonRequestAttack` |
| **Order** | 10 |
| **Intent** | Verify the server **rejects syntactically invalid JSON** on create; accepting such bodies with **200/201** is treated as over-permissive. |

**Requests**

| # | Method | Path | Body |
|---|--------|------|------|
| 1 | `POST` | `/Patient` | Truncated JSON: `{ "resourceType": "Patient", ` (incomplete object) |
| 2 | `POST` | `/Patient` | Invalid JSON: `{ "resourceType": "Patient", }` (trailing comma before `}`) |

**Classification (summary)**  
`AttackOutcome.validationPost`: **400 / 404 / 405 / 412 / 422** → SECURE; **200 / 201** → VULNERABLE; **401 / 403** → SECURE (auth required); **500** → INCONCLUSIVE.  
Both steps are combined with **`combineWorstAll`** (worst outcome wins).

---

## 2. Metadata Manipulation

| | |
|---|---|
| **Display name** | `Metadata Manipulation` |
| **Java class** | `MetadataManipulationAttack` |
| **Order** | 20 |
| **Intent** | Probe **weak semantic validation**: wrong `meta` types, fake `resourceType`, suspicious `id` / `identifier`. |

**Requests** (each `POST /Patient`)

| # | Body (JSON) | Notes |
|---|-------------|--------|
| 1 | `{"resourceType":"Patient","meta":{"versionId":123}}` | `versionId` must be a **string** in FHIR; numeric tests type rules. |
| 2 | `{"resourceType":"FakeResource","id":"1"}` | Wrong `resourceType` for the Patient endpoint. |
| 3 | `{"resourceType":"Patient","id":"visible\u0000hidden-data","identifier":[{"system":"urn:test","value":"id;secret=1"}]}` | Null byte in `id`; odd `identifier` value. |

**Classification**  
Same `validationPost` rules as above; **three** results merged with **`combineWorstAll`**.

---

## 3. Unexpected Payload Injection

| | |
|---|---|
| **Display name** | `Unexpected Payload Injection` |
| **Java class** | `UnexpectedPayloadInjectionAttack` |
| **Order** | 30 |
| **Intent** | Test tolerance for **unknown fields**, **duplicate JSON keys**, and **extra nested** properties (parser / validation strictness). |

**Requests** (each `POST /Patient`)

| # | Body (JSON) |
|---|-------------|
| 1 | `{"resourceType":"Patient","name":[{"family":"Test"}],"unknownField":"should-reject","__proto__":{}}` |
| 2 | `{"resourceType":"Patient","id":"valid-id","id":"duplicate-id"}` |
| 3 | `{"resourceType":"Patient","name":[{"family":"Test"}],"_payload":"hidden","extraNested":{"secret":"data"}}` |

**Classification**  
`validationPost` × 3, **`combineWorstAll`**.

---

## 4. Extension Fields Misuse

| | |
|---|---|
| **Display name** | `Extension Fields Misuse` |
| **Java class** | `ExtensionFieldsMisuseAttack` |
| **Order** | 40 |
| **Intent** | Send a **non-standard extension** with a “covert” string to see if the server **stores** it on a created Patient (potential covert channel). |

**Request**

| Method | Path | Body |
|--------|------|------|
| `POST` | `/Patient` | `{"resourceType":"Patient","name":[{"family":"Test"}],"extension":[{"url":"http://malicious.example/hidden","valueString":"covert-payload"}]}` |

**Classification**  
Single `validationPost` on the response.

---

## 5. Contained Resource Smuggling

| | |
|---|---|
| **Display name** | `Contained Resource Smuggling` |
| **Java class** | `ContainedResourceSmugglingAttack` |
| **Order** | 50 |
| **Intent** | POST a Patient embedding a **`contained`** `Binary` with base64 payload to detect **smuggling** of nested data via contained resources. |

**Request**

| Method | Path | Body |
|--------|------|------|
| `POST` | `/Patient` | `{"resourceType":"Patient","contained":[{"resourceType":"Binary","id":"covert","contentType":"text/plain","data":"c2VjcmV0LWRhdGE="}]}` |

`c2VjcmV0LWRhdGE=` decodes to ASCII `secret-data`.

**Classification**  
`validationPost`.

---

## 6. Encoded Hidden Data

| | |
|---|---|
| **Display name** | `Encoded Hidden Data` |
| **Java class** | `EncodedHiddenDataAttack` |
| **Order** | 60 |
| **Intent** | Use **Unicode escapes** in `meta.tag.display` so the decoded display reads **Secret**—tests whether encoded tags are stored literally or normalized and accepted. |

**Request**

| Method | Path | Body |
|--------|------|------|
| `POST` | `/Patient` | `{"resourceType":"Patient","meta":{"tag":[{"code":"x","display":"\u0053\u0065\u0063\u0072\u0065\u0074"}]}}` |

(`\u0053\u0065…` → `Secret` after JSON parsing.)

**Classification**  
`validationPost`.

---

## 7. Invalid Credentials Access Test

| | |
|---|---|
| **Display name** | `Invalid Credentials Access Test` |
| **Java class** | `InvalidCredentialsAccessAttack` |
| **Order** | 70 |
| **Intent** | Probe **OAuth token endpoint** (if discoverable), then **GET** with invalid **Basic** and forged / malformed **Bearer** tokens. Success (**200/201**) with bad credentials while OAuth is **advertised** → VULNERABLE. |

**Sub-probes**

### 7a. Token endpoint (conditional)

| Step | Method | Path / URL | Body / headers |
|------|--------|------------|----------------|
| Discover | `GET` | `{base}/.well-known/smart-configuration` | — |
| Discover | `GET` | `{base}/metadata` | — |
| If `token_endpoint` found | `POST` | Token URL (from discovery) | `application/x-www-form-urlencoded`: `grant_type=client_credentials&client_id=invalid_week7_probe_client` |

If no token URL is found, this sub-probe is **INCONCLUSIVE** with note `No token_endpoint discovered.`

### 7b. Invalid Basic on read

| Method | Path | Header |
|--------|------|--------|
| `GET` | `/Patient?_count=1` | `Authorization: Basic aW52YWxpZDppbnZhbGlk` |

(`aW52YWxpZDppbnZhbGlk` is Base64 for `invalid:invalid`.)

### 7c. Forged Bearer (synthetic JWT)

Same `GET` URLs with header:

`Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid_signature_week7`

- Called for **`/Patient?_count=1`** and **`/Observation?_count=1`**.

### 7d. Malformed Bearer variants

`GET {base}/Patient?_count=1` three times with:

1. `Authorization: Bearer `  
2. `Authorization: Bearer not.a.valid.jwt.week7`  
3. `Authorization: Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJleHAiOjE1MDAwMDAwMDAsInN1YiI6IndlZWs3In0.x` (expired-shaped / weak structure)

**Classification**  
`authReadWithBadCredentials` / token rules; all branches merged with **`combineWorstAll`**.

---

## 8. Open Endpoint Detection

| | |
|---|---|
| **Display name** | `Open Endpoint Detection` |
| **Java class** | `OpenEndpointDetectionAttack` |
| **Order** | 80 |
| **Intent** | Compare **advertised OAuth/SMART** (from well-known + CapabilityStatement) with **unauthenticated** `GET /Patient?_count=1`. Mismatch → **MISCONFIGURED** or **OPEN_POLICY** / **SECURE** depending on metadata. |

**Requests**

| # | Method | Path |
|---|--------|------|
| 1 | `GET` | `{base}/.well-known/smart-configuration` |
| 2 | `GET` | `{base}/metadata` |
| 3 | `GET` | `{base}/Patient?_count=1` |

No `Authorization` header on any call.

**Classification**  
Logic in `OpenEndpointDetectionAttack`: if OAuth advertised and Patient read returns **200/201** → **MISCONFIGURED**; if not advertised and **200/201** → **OPEN_POLICY**; **401/403** on read with advertised OAuth → **SECURE**; etc.

---

## 9. Cross-Patient Access

| | |
|---|---|
| **Display name** | `Cross-Patient Access` |
| **Java class** | `CrossPatientAccessAttack` |
| **Order** | 90 |
| **Intent** | After creating a **victim** Patient (and an Observation on that subject), perform **unauthenticated reads** as if an attacker obtained the victim id—**IDOR-style** read across patient context. |

**Setup writes** (via `AbstractAccessControlAttack`)

1. `POST /Patient` — attacker-like Patient: `{"resourceType":"Patient","name":[{"given":["Attacker-<token>"],"family":"PatientA-<token>"}]}`  
2. `POST /Patient` — victim: `{"resourceType":"Patient","name":[{"given":["Victim-<token>"],"family":"PatientB-<token>"}]}`  
3. Extract victim **id** from response.

**Probe reads** (no auth)

| Method | Path |
|--------|------|
| `GET` | `/Patient/{victimId}` |
| `POST /Observation` | Creates Observation with `subject` = `Patient/{victimId}`, LOINC `718-7`, `valueString` like `CrossPatientProbe-<token>`. |
| `GET` | `/Observation?subject=` + URL-encoded `Patient/{victimId}` |

**Classification**  
`AttackOutcome.crossPatientRead` per read, then **`combineWorst`** for Patient vs Observation.

---

## 10. Unauthorized Write / ID Tampering

| | |
|---|---|
| **Display name** | `Unauthorized Write / ID Tampering` |
| **Java class** | `UnauthorizedWriteIdTamperingAttack` |
| **Order** | 100 |
| **Intent** | **PUT** tampering on an existing Patient; **POST** Observation tied to a victim subject, then **GET** that Observation—tests **anonymous or weakly authenticated writes**. |

### Sub-scenario A — ID tampering (`runIdTampering`)

1. `POST /Patient` — victim: names `Victim-<token>`, `PatientVictim-<token>`.  
2. `PUT /Patient/{victimId}` with body:

```json
{
  "resourceType": "Patient",
  "id": "<victimId>",
  "name": [{
    "given": ["Tampered-<token>"],
    "family": "TamperedFamily-<token>"
  }]
}
```

### Sub-scenario B — Observation chain (`runOwnerReferenceObservation`)

1. `POST /Patient` — victim: `Victim-<token>`, `PatientOwnerRef-<token>`.  
2. `POST /Observation` — same shape as helper in `AbstractAccessControlAttack`:

   - `resourceType` `Observation`, `status` `final`  
   - `code`: text `SecurityTest`, coding `http://loinc.org` / `718-7`  
   - `subject.reference`: `Patient/{victimId}`  
   - `valueString`: `OwnerRefProbe-<token>`

3. `GET /Observation/{observationId}`

**Classification**  
`anonymousWrite` on PUT and POST; second part augments reason with follow-up GET status. Overall **`combineWorst`** of both sub-scenarios.

---

## How results are stored

Each scenario produces one `AttackResult` with HTTP status, **`responseBody`** (possibly concatenated sub-probes with `---` separators), **`AttackClassification`**, reason, and **`AttackSeverity`**. See `AttackOutcome` for exact status→classification mapping (`validationPost`, `authReadWithBadCredentials`, `anonymousWrite`, `crossPatientRead`, etc.).

---

## Related documentation

- Per-server measured outcomes: `hapi-public.md`, `firely.md`, `azure-healthcare-apis.md`, `smarthealthit.md`  
- Summary table: `reports/analysis-runs-31-34-summary.tsv`
