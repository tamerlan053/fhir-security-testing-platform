# Attack run — HAPI Public

- **Base URL:** `http://hapi.fhir.org/baseR4`
- **Test run ID:** 11
- **Started at:** 2026-05-26T13:43:24.078035
- **Scenarios:** 12
- **VULNERABLE count:** 2
- **Non-NONE leakage:** 1

## Summary table

| # | Scenario | Status | Classification | Severity | Leakage | Vulnerable |
|---|----------|--------|----------------|----------|---------|------------|
| 1 | Malformed JSON Request | 400 | `SECURE` | INFO | `NONE` | False |
| 2 | Metadata Manipulation | 412 | `SECURE` | INFO | `NONE` | False |
| 3 | Unexpected Payload Injection | 201 | `SECURE` | INFO | `NONE` | False |
| 4 | Extension Fields Misuse | 201 | `VULNERABLE` | MEDIUM | `NONE` | True |
| 5 | Contained Resource Smuggling | 201 | `VULNERABLE` | MEDIUM | `NONE` | True |
| 6 | Encoded Hidden Data | 412 | `SECURE` | INFO | `NONE` | False |
| 7 | Invalid Credentials Access Test | 200 | `INCONCLUSIVE` | LOW | `NONE` | False |
| 8 | Open Endpoint Detection | 200 | `OPEN_POLICY` | INFO | `NONE` | False |
| 9 | Authenticated Token Isolation | 0 | `INCONCLUSIVE` | LOW | `NONE` | False |
| 10 | Cross-Patient Access | 200 | `OPEN_POLICY` | INFO | `NONE` | False |
| 11 | Observation Bundle / Duplicate Clinical | 302 | `INCONCLUSIVE` | LOW | `NONE` | False |
| 12 | Unauthorized Write / ID Tampering | 200 | `OPEN_POLICY` | INFO | `VERBOSE_ERROR_BODY` | False |

## Detailed results

### Malformed JSON Request

| Field | Value |
|-------|-------|
| **testResultId** | 109 |
| **HTTP status** | 400 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,json.syntax_invalid,json.truncation` |

**Reason:**

Truncated JSON: server rejected malformed JSON as expected. | Also: SECURE — Trailing comma JSON: server rejected malformed JSON as expected.

**Request log:**

```http
POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{ "resourceType": "Patient",

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{ "resourceType": "Patient", }
```

**Response body:**

```
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

---
### Metadata Manipulation

| Field | Value |
|-------|-------|
| **testResultId** | 110 |
| **HTTP status** | 412 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.meta_semantics,fhir.resourceType,fhir.id_identifier` |

**Reason:**

Server rejected invalid meta.versionId type as expected. | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server rejected suspicious client-supplied id as expected.

**Request log:**

```http
POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","meta":{"versionId":123}}

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"FakeResource","id":"1"}

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","id":"visible\u0000hidden-data"}
```

**Response body:**

```
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/132087500</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/132087500"
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

---
### Unexpected Payload Injection

| Field | Value |
|-------|-------|
| **testResultId** | 111 |
| **HTTP status** | 201 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,json.unknown_fields,json.duplicate_keys,json.extra_nested` |

**Reason:**

unknownField/__proto__ injection: marker not present on follow-up GET (server likely ignored/stripped unexpected fields). | Also: SECURE — Duplicate key: server rejected ambiguous JSON as expected. | Also: SECURE — _payload/extraNested injection: server rejected the payload.

**Request log:**

```http
POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Test"}],"unknownField":"inj-unknown-b9cf6ea299","__proto__":{"polluted":"inj-unknown-b9cf6ea299"}}

---

GET http://hapi.fhir.org/baseR4/Patient/132088828

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","id":"valid-id","id":"duplicate-id"}

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Test"}],"_payload":"inj-nested-aae8ea4310","extraNested":{"secret":"inj-nested-aae8ea4310"}}
```

**Response body:**

```
unknownField/__proto__ injection POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "132088828",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T11:43:29.434+00:00",
    "source": "#u9dGQYDs8rS9TT83"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\"><b>TEST </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "name": [ {
    "family": "Test"
  } ]
}

Follow-up GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.headerValue {
   color: #88F;
   font-family: monospace;
}

.narrativeBody {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.narrativeBody DIV,
.narrativeBody TABLE {
   font-size: 0.9em;
}

.narrativeBody TABLE > THEAD {
   background: #AAA;
}

.narrativeBody TABLE > TBODY > TR:nth-child(odd) > TD {
   background: #CCC;
}

.narrativeBody TABLE > TBODY > TR:nth-child(even) > TD {
   background: #EEE;
}

.narrativeBody TABLE TD, .narrativeBody TABLE TH {
   padding: 5px;
}

.responseBodyTable {
   width: 100%;
   margin-left: 0px;
   margin-top: -10px;
   position: relative;
}

.responseBodyTableFirstColumn {
}

.responseBodyTableSecondColumn {
   p…
---
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/132087500</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/132087500"
  } ]
}
---
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/132088828</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/132088828"
  } ]
}
```

---
### Extension Fields Misuse

| Field | Value |
|-------|-------|
| **testResultId** | 112 |
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
POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Probe-covert-ext-6666b0230c2e"}],"extension":[{"url":"http://malicious.example/hidden","valueString":"covert-ext-6666b0230c2e"}]}

---

GET http://hapi.fhir.org/baseR4/Patient/132088831
```

**Response body:**

```
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "132088831",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T11:43:33.433+00:00",
    "source": "#TExJUfM5SNGliUWz"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\"><b>PROBE-COVERT-EXT-6666B0230C2E </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "extension": [ {
    "url": "http://malicious.example/hidden",
    "valueString": "covert-ext-6666b0230c2e"
  } ],
  "name": [ {
    "family": "Probe-covert-ext-6666b0230c2e"
  } ]
}

GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.headerValue {
   color: #88F;
   font-family: monospace;
}

.narrativeBody {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.narrativeBody DIV,
.narrativeBody TABLE {
   font-size: 0.9em;
}

.narrativeBody TABLE > THEAD {
   background: #AAA;
}

.narrativeBody TABLE > TBODY > TR:nth-child(odd) > TD {
   background: #CCC;
}

.narrativeBody TABLE > TBODY > TR:nth-child(even) > TD {
   background: #EEE;
}

.narrativeBody TABLE TD, .narrativeBody TABLE TH {
   padding: 5px;
}

.responseBodyTable {
   width: 100%;
   margin-left: 0px;
   margin-top: -10px…
```

---
### Contained Resource Smuggling

| Field | Value |
|-------|-------|
| **testResultId** | 113 |
| **HTTP status** | 201 |
| **Classification** | `VULNERABLE` |
| **Vulnerable** | True |
| **Severity** | MEDIUM |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.contained.binary,fhir.base64_payload` |

**Reason:**

Contained Binary marker persisted and is retrievable (potential nested payload smuggling channel).

**Request log:**

```http
POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Probe-covert-bin-c5cd1c1e400c"}],"contained":[{"resourceType":"Binary","id":"covert","contentType":"text/plain","data":"Y292ZXJ0LWJpbi1jNWNkMWMxZTQwMGM="}]}

---

GET http://hapi.fhir.org/baseR4/Patient/132088832
```

**Response body:**

```
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "132088832",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T11:43:35.434+00:00",
    "source": "#vk6LBUVzMsQTM7sM"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\"><b>PROBE-COVERT-BIN-C5CD1C1E400C </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "contained": [ {
    "resourceType": "Binary",
    "id": "covert",
    "contentType": "text/plain",
    "data": "Y292ZXJ0LWJpbi1jNWNkMWMxZTQwMGM="
  } ],
  "name": [ {
    "family": "Probe-covert-bin-c5cd1c1e400c"
  } ]
}

GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.headerValue {
   color: #88F;
   font-family: monospace;
}

.narrativeBody {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.narrativeBody DIV,
.narrativeBody TABLE {
   font-size: 0.9em;
}

.narrativeBody TABLE > THEAD {
   background: #AAA;
}

.narrativeBody TABLE > TBODY > TR:nth-child(odd) > TD {
   background: #CCC;
}

.narrativeBody TABLE > TBODY > TR:nth-child(even) > TD {
   background: #EEE;
}

.narrativeBody TABLE TD, .narrativeBody TABLE TH {
   padding: 5px;
}

.responseBodyTable {
   width: 100%;
   margin-left: 0px;
   margin-top: -10px…
```

---
### Encoded Hidden Data

| Field | Value |
|-------|-------|
| **testResultId** | 114 |
| **HTTP status** | 412 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.meta.tag,unicode.normalization_display` |

**Reason:**

Server rejected the payload (no encoded marker persistence).

**Request log:**

```http
POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","meta":{"tag":[{"code":"x","display":"\u0063\u006f\u0076\u0065\u0072\u0074\u002d\u0074\u0061\u0067\u002d\u0064\u0064\u0032\u0031\u0063\u0062\u0032\u0039\u0039\u0066\u0031\u0066"}]}}
```

**Response body:**

```
{
  "resourceType": "OperationOutcome",
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><h1>Operation Outcome</h1><table border=\"0\"><tr><td style=\"font-weight: bold;\">ERROR</td><td>[]</td><td>HAPI-2840: Can not create resource duplicating existing resource: Patient/132087500</td></tr></table></div>"
  },
  "issue": [ {
    "severity": "error",
    "code": "processing",
    "diagnostics": "HAPI-2840: Can not create resource duplicating existing resource: Patient/132087500"
  } ]
}
```

---
### Invalid Credentials Access Test

| Field | Value |
|-------|-------|
| **testResultId** | 115 |
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
GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata

---

GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata

---

GET http://hapi.fhir.org/baseR4/Patient?_count=1
Authorization: Basic aW52YWxpZDppbnZhbGlk

---

GET http://hapi.fhir.org/baseR4/Patient?_count=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid_signature_week7

---

GET http://hapi.fhir.org/baseR4/Observation?_count=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid_signature_week7

---

GET http://hapi.fhir.org/baseR4/Patient?_count=1
Authorization: Bearer

---

GET http://hapi.fhir.org/baseR4/Patient?_count=1
Authorization: Bearer not.a.valid.jwt.week7

---

GET http://hapi.fhir.org/baseR4/Patient?_count=1
Authorization: Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJleHAiOjE1MDAwMDAwMDAsInN1YiI6IndlZWs3In0.x
```

**Response body:**

```
No token_endpoint discovered.
---
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.headerValue {
   color: #88F;
   font-family: monospace;
}

.narrativeBody {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.narrativeBody DIV,
.narrativeBody TABLE {
   font-size: 0.9em;
}

.narrativeBody TABLE > THEAD {
   background: #AAA;
}

.narrativeBody TABLE > TBODY > TR:nth-child(odd) > TD {
   background: #CCC;
}

.narrativeBody TABLE > TBODY > TR:nth-child(even) > TD {
   background: #EEE;
}

.narrativeBody TABLE TD, .narrativeBody TABLE TH {
   padding: 5px;
}

.responseBodyTable {
   width: 100%;
   margin-left: 0px;
   margin-top: -10px;
   position: relative;
}

.responseBodyTableFirstColumn {
}

.responseBodyTableSecondColumn {
   p…
---
Bearer probes Patient 200 | Observation 200
---
Bearer variant → HTTP 200; Bearer variant → HTTP 200; Bearer variant → HTTP 200;
```

---
### Open Endpoint Detection

| Field | Value |
|-------|-------|
| **testResultId** | 116 |
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
GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata

---

GET http://hapi.fhir.org/baseR4/Patient?_count=1
```

**Response body:**

```
well-known HTTP 404, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: <html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.headerValue {
   color: #88F;
   font-family: monospace;
}

.narrativeBody {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.narrativeBody DIV,
.narrativeBody TABLE {
   font-size: 0.9em;
}

.narrativeBody TABLE > THEAD {
   background: #AAA;
}

.narrativeBody TABLE > TBODY > TR:nth-child(odd) > TD {
   background: #CCC;
}

.narrativeBody TABLE > TBODY >…
```

---
### Authenticated Token Isolation

| Field | Value |
|-------|-------|
| **testResultId** | 117 |
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
GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata
```

**Response body:**

_empty_

---
### Cross-Patient Access

| Field | Value |
|-------|-------|
| **testResultId** | 118 |
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
GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Attacker-2d5e1cf5bdaf"],"family":"PatientA-2d5e1cf5bdaf"}]}

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-18527f473c26"],"family":"PatientB-18527f473c26"}]}

---

GET http://hapi.fhir.org/baseR4/Patient/132088835

---

POST http://hapi.fhir.org/baseR4/Observation
Content-Type: application/json

{"resourceType":"Observation","status":"final","code":{"text":"SecurityTest","coding":[{"system":"http://loinc.org","code":"718-7"}]},"subject":{"reference":"Patient/132088835"},"valueString":"CrossPatientProbe-ba0a504e5150"}

---

GET http://hapi.fhir.org/baseR4/Observation?subject=Patient%2F132088835
```

**Response body:**

```
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

---
### Observation Bundle / Duplicate Clinical

| Field | Value |
|-------|-------|
| **testResultId** | 119 |
| **HTTP status** | 302 |
| **Classification** | `INCONCLUSIVE` |
| **Vulnerable** | False |
| **Severity** | LOW |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `fhir.bundle.transaction,http.post.observation_batch,fhir.observation.duplicate_clinical` |

**Reason:**

Unexpected HTTP status on bundle transaction: 302

**Request log:**

```http
GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["BundleDup-c14c185a8204"],"family":"ClinicalSubject-c14c185a8204"}]}

---

POST http://hapi.fhir.org/baseR4
Content-Type: application/json

{"resourceType":"Bundle","type":"transaction","entry":[{"fullUrl":"urn:uuid:week11-dup-0-4ab8e389c875","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/132088837"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-4ab8e389c875"}]},"request":{"method":"POST","url":"Observation"}},{"fullUrl":"urn:uuid:week11-dup-1-4ab8e389c875","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/132088837"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-4ab8e389c875"}]},"request":{"method":"POST","url":"Observation"}},{"fullUrl":"urn:uuid:week11-dup-2-4ab8e389c875","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/132088837"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-4ab8e389c875"}]},"request":{"method":"POST","url":"Observation"}}]}
```

**Response body:**

```
POST transaction Bundle (3 duplicate-structure Observations for Patient/132088837) → HTTP 302. Created-like entry responses: 0.
```

---
### Unauthorized Write / ID Tampering

| Field | Value |
|-------|-------|
| **testResultId** | 120 |
| **HTTP status** | 200 |
| **Classification** | `OPEN_POLICY` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `VERBOSE_ERROR_BODY` |
| **Attack vectors** | `http.put.patient,http.post.observation,http.get.observation,auth.anonymous_write` |

**Reason:**

Anonymous write succeeded — common on open sandboxes for testing (no OAuth advertised). Follow-up GET shows tampered marker persisted. | Also: OPEN_POLICY — Anonymous write succeeded — common on open sandboxes for testing (no OAuth advertised). Follow-up GET HTTP 200.

**Request log:**

```http
GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-e7358e7140e8"],"family":"PatientVictim-e7358e7140e8"}]}

---

GET http://hapi.fhir.org/baseR4/Patient/132088838

---

PUT http://hapi.fhir.org/baseR4/Patient/132088838
Content-Type: application/json

{"resourceType":"Patient","id":"132088838","name":[{"given":["Tampered-c2125d257893"],"family":"TamperedFamily-c2125d257893"}]}

---

GET http://hapi.fhir.org/baseR4/Patient/132088838

---

GET http://hapi.fhir.org/baseR4/.well-known/smart-configuration

---

GET http://hapi.fhir.org/baseR4/metadata

---

POST http://hapi.fhir.org/baseR4/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-dfd0d7604ae2"],"family":"PatientOwnerRef-dfd0d7604ae2"}]}

---

POST http://hapi.fhir.org/baseR4/Observation
Content-Type: application/json

{"resourceType":"Observation","status":"final","code":{"text":"SecurityTest","coding":[{"system":"http://loinc.org","code":"718-7"}]},"subject":{"reference":"Patient/132088840"},"valueString":"OwnerRefProbe-1011aae12c27"}

---

GET http://hapi.fhir.org/baseR4/Observation/132088841
```

**Response body:**

```
GET /Patient/{id} before PUT (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.hea…

PUT /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "132088838",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-05-26T11:44:01.666+00:00",
    "source": "#TnvoJ6PTRp0AuA6Q"
  },
  "text": {
    "status": "generated",
    "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Tampered-c2125d257893 <b>TAMPEREDFAMILY-C2125D257893 </b></div><table class=\"hapiPropertyTable\"><tbody/></table></div>"
  },
  "name": [ {
    "family": "TamperedFamily-c2125d257893",
    "given": [ "Tampered-c2125d257893" ]
  } ]
}

GET /Patient/{id} after PUT (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.headerValue {
   color: #88F;
   font-family: monospace;
}

.narrativeBody {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.narrativeBody DIV,
.narrativeBody TABLE {
   font-size: 0.9em;
}

.narrativeBody TABLE > THEAD {
   background: #AAA;
}

.narrativeBody TABLE > TBODY > TR:nth-child(odd) > TD {
   background: #CCC;
}

.narrativeBody TABLE > TBODY >…

POST /Observation:
{
  "resourceType": "Observation",
  "id": "132088841",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T11:44:06.046+00:00",
    "source": "#HKtqsAyhuo9CYKjn"
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
    "reference": "Patient/132088840"
  },
  "valueString": "OwnerRefProbe-1011aae12c27"
}

GET /Observation/{id}:
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {
   font-size: 1.2em;
   font-weight: bold;
}

.hlQuot {
   color: #88F;
}

.hlQuot a {
   text-decoration: underline;
   text-decoration-color: #CCC;
}

.hlQuot a:HOVER {
   text-decoration: underline;
   text-decoration-color: #008;
}

.hlQuot .uuid, .hlQuot .dateTime {
   user-select: all;
   -moz-user-select: all;
   -webkit-user-select: all;
   -ms-user-select: element;
}

.hlAttr {
   color: #888;
}

.hlTagName {
   color: #006699;
}

.hlControl {
   color: #660000;
}

.hlText {
   color: #000000;
}

.hlUrlBase {
}

.headersDiv {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.headersRow {
}

.headerName {
   color: #888;
   font-family: monospace;
}

.headerValue {
   color: #88F;
   font-family: monospace;
}

.narrativeBody {
   padding: 10px;
   margin-left: 10px;
   border: 1px solid #CCC;
   border-radius: 10px;
}

.narrativeBody DIV,
.narrativeBody TABLE {
   font-size: 0.9em;
}

.narrativeBody TABLE > THEAD {
   background: #AAA;
}

.narrativeBody TABLE > TBODY > TR:nth-child(odd) > TD {
   background: #CCC;
}

.narrativeBody TABLE > TBODY > TR:nth-child(even) > TD {
   background: #EEE;
}

.narrativeBody TABLE TD, .narrativeBody TABLE TH {
   padding: 5px;
}

.responseBodyTable {
   width: 100%;
   margin-left: 0px;
   margin-top: -10px;
   position: relative;
}

.responseBodyTableFirstColumn {
}

.responseBodyTableSecondColumn {
   position: absolute;
   margin-left: 70px;
   vertical-align: top;
   left: 0px;
   right: 0px;
}

.responseBodyTableSecondColumn PRE {
   margin: 0px;
}

.sizeInfo {
   margin-top: 20px;
   font-size: 0.8em;
}

.lineAnchor A {
   text-decoration: none;
   padding-left: 20px;
}

.lineAnchor {
   display: block;
   padding-right: 20px;
}

.selectedLine {
   background-color: #EEF;
   font-weight: bold;
}

H1 {
   font-size: 1.1em;
   color: #666;
}

BODY {
   font-family: Arial;
}
       </style>
	</head>

	<body><p>This result is being rendered in HTML for easy viewing. You may access this content as <a href="?_format=json">Raw JSON</a> or <a href="?_format=xml">Raw XML</a> or <a href="?_format=ttl">Raw Turtle</a> or view this content in <a href="?_format=html/json">HTML JSON</a> or <a href="?_format=html/xml">HTML XML</a> or <a href="?_format=html/turtle">HTML Turtle</a> . Response generated in 4ms.</p>
<div class="httpStatusDiv">HTTP 200 OK</div>

<h1>Response Headers</h1><div class="headersDiv"><div class="headersRow"><span class="headerName">Server: </span><span class="headerValue">Jetty(12.0.8)</span></div><div class="headersRow"><span class="headerName">X-Powered-By: </span><span class="headerValue">HAPI FHIR 8.11.4-SNAPSHOT/6416207ae2/2026-05-19 REST Server (FHIR Server; FHIR 4.0.1/R4)</span></div><div class="headersRow"><span class="headerName">Content-Type: </span><span class="headerValue">text/html;charset=utf-8</span></div><div class="headersRow"><span class="headerName">X-Request-ID: </span><span class="headerValue">VbyREnJjkVZycD8V</span></div></div><h1>Response Body</h1><div class="responseBodyTable"><div class="responseBodyTableSecondColumn"><pre><div id="line1"><span class='hlControl'>{</span></div><div id="line2" onclick="updateHighlightedLineTo('#L2');">  <span class='hlTagName'>&quot;resourceType&quot;</span>: <span class='hlQuot'>&quot;Observation&quot;</span><span class='hlControl'>,</span></div><div id="line3" onclick="updateHighlightedLineTo('#L3');">  <span class='hlTagName'>&quot;id&quot;</span>: <span class='hlQuot'>&quot;132088841&quot;</span><span class='hlControl'>,</span></div><div id="line4" onclick="updateHighlightedLineTo('#L4');">  <span class='hlTagName'>&quot;meta&quot;</span>: <span class='hlControl'>{</span></div><div id="line5" onclick="updateHighlightedLineTo('#L5');">    <span class='hlTagName'>&quot;versionId&quot;</span>: <span class='hlQuot'>&quot;1&quot;</span><span class='hlControl'>,</span></div><div id="line6" onclick="updateHighlightedLineTo('#L6');">    <span class='hlTagName'>&quot;lastUpdated&quot;</span>: <span class='hlQuot'>&quot;2026-05-26T11:44:06.046+00:00&quot;</span><span class='hlControl'>,</span></div><div id="line7" onclick="updateHighlightedLineTo('#L7');">    <span class='hlTagName'>&quot;source&quot;</span>: <span class='hlQuot'>&quot;#HKtqsAyhuo9CYKjn&quot;</span></div><div id="line8" onclick="updateHighlightedLineTo('#L8');">  <span class='hlControl'>}</span><span class='hlControl'>,</span></div><div id="line9" onclick="updateHighlightedLineTo('#L9');">  <span class='hlTagName'>&quot;status&quot;</span>: <span class='hlQuot'>&quot;final&quot;</span><span class='hlControl'>,</span></div><div id="line10" onclick="updateHighlightedLineTo('#L10');">  <span class='hlTagName'>&quot;code&quot;</span>: <span class='hlControl'>{</span></div><div id="line11" onclick="updateHighlightedLineTo('#L11');">    <span class='hlTagName'>&quot;coding&quot;</span>: <span class='hlControl'>[</span> <span class='hlControl'>{</span></div><div id="line12" onclick="updateHighlightedLineTo('#L12');">      <span class='hlTagName'>&quot;system&quot;</span>: <span class='hlQuot'>&quot;http://loinc.org&quot;</span><span class='hlControl'>,</span></div><div id="line13" onclick="updateHighlightedLineTo('#L13');">      <span class='hlTagName'>&quot;code&quot;</span>: <span class='hlQuot'>&quot;718-7&quot;</span></div><div id="line14" onclick="updateHighlightedLineTo('#L14');">    <span class='hlControl'>}</span> ]<span class='hlControl'>,</span></div><div id="line15" onclick="updateHighlightedLineTo('#L15');">    <span class='hlTagName'>&quot;text&quot;</span>: <span class='hlQuot'>&quot;SecurityTest&quot;</span></div><div id="line16" onclick="updateHighlightedLineTo('#L16');">  <span class='hlControl'>}</span><span class='hlControl'>,</span></div><div id="line17" onclick="updateHighlightedLineTo('#L17');">  <span class='hlTagName'>&quot;subject&quot;</span>: <span class='hlControl'>{</span></div><div id="line18" onclick="updateHighlightedLineTo('#L18');">    <span class='hlTagName'>&quot;reference&quot;</span>: <span class='hlQuot'>&quot;Patient/132088840&quot;</span></div><div id="line19" onclick="updateHighlightedLineTo('#L19');">  <span class='hlControl'>}</span><span class='hlControl'>,</span></div><div id="line20" onclick="updateHighlightedLineTo('#L20');">  <span class='hlTagName'>&quot;valueString&quot;</span>: <span class='hlQuot'>&quot;OwnerRefProbe-1011aae12c27&quot;</span></div><div id="line21" onclick="updateHighlightedLineTo('#L21');"><span class='hlControl'>}</span></div></pre></div><div class="responseBodyTableFirstColumn"><pre><div class="lineAnchor" id="anchor1"><a href="#L1" id="L1">1</a></div><div class="lineAnchor" id="anchor2"><a href="#L2" id="L2">2</a></div><div class="lineAnchor" id="anchor3"><a href="#L3" id="L3">3</a></div><div class="lineAnchor" id="anchor4"><a href="#L4" id="L4">4</a></div><div class="lineAnchor" id="anchor5"><a href="#L5" id="L5">5</a></div><div class="lineAnchor" id="anchor6"><a href="#L6" id="L6">6</a></div><div class="lineAnchor" id="anchor7"><a href="#L7" id="L7">7</a></div><div class="lineAnchor" id="anchor8"><a href="#L8" id="L8">8</a></div><div class="lineAnchor" id="anchor9"><a href="#L9" id="L9">9</a></div><div class="lineAnchor" id="anchor10"><a href="#L10" id="L10">10</a></div><div class="lineAnchor" id="anchor11"><a href="#L11" id="L11">11</a></div><div class="lineAnchor" id="anchor12"><a href="#L12" id="L12">12</a></div><div class="lineAnchor" id="anchor13"><a href="#L13" id="L13">13</a></div><div class="lineAnchor" id="anchor14"><a href="#L14" id="L14">14</a></div><div class="lineAnchor" id="anchor15"><a href="#L15" id="L15">15</a></div><div class="lineAnchor" id="anchor16"><a href="#L16" id="L16">16</a></div><div class="lineAnchor" id="anchor17"><a href="#L17" id="L17">17</a></div><div class="lineAnchor" id="anchor18"><a href="#L18" id="L18">18</a></div><div class="lineAnchor" id="anchor19"><a href="#L19" id="L19">19</a></div><div class="lineAnchor" id="anchor20"><a href="#L20" id="L20">20</a></div><div class="lineAnchor" id="anchor21"><a href="#L21" id="L21">21</a></div></div></td></div>
<script type="text/javascript">let selectedLines = [];

function updateHighlightedLine() {
	updateHighlightedLineTo(window.location.hash);
}

function updateHighlightedLineTo(theNewHash) {

   let next;
   for (next in selectedLines) {
		document.getElementById('line' + selectedLines[next]).className = '';
		document.getElementById('anchor' + selectedLines[next]).className = 'lineAnchor';
	}
	selectedLines = [];

   let line = -1;
   if (theNewHash && theNewHash.match('L[0-9]+-L[0-9]+')) {
      const dashIndex = theNewHash.indexOf('-');
      const start = parseInt(theNewHash.substring(2, dashIndex));
      const end = parseInt(theNewHash.substring(dashIndex + 2));
      for (let i = start; i <= end; i++) {
			selectedLines.push(i);
		}
	} else if (theNewHash && theNewHash.match('L[0-9]+')) {
		line = parseInt(theNewHash.substring(2));
		selectedLines.push(line);
	}


	for (next in selectedLines) {
		// Prevent us from scrolling to the selected line
		document.getElementById('L' + selectedLines[next]).name = '';
		// Select the line number column
		document.getElementById('line' + selectedLines[next]).className = 'selectedLine';
		// Select the response body column
		document.getElementById('anchor' + selectedLines[next]).className = 'lineAnchor selectedLine';
	}
		
	selectedLine = line;
}

function updateHyperlinksAndStyles() {
    /* adds hyperlinks and CSS styles to dates and UUIDs (e.g. to enable user-select: all) */
    const logicalReferenceRegex = /^[A-Z][A-Za-z]+\/[0-9]+$/;
    const dateTimeRegex = /^-?[0-9]{4}(-(0[1-9]|1[0-2])(-(0[0-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\.[0-9]+)?(Z|([+\-])((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?$/; // from the spec - https://www.hl7.org/fhir/datatypes.html#datetime
    const uuidRegex = /^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$/;

    const allQuotes = document.querySelectorAll(".hlQuot");
    for (var i = 0; i < allQuotes.length; i++) {
        const quote = allQuotes[i];
        const text = quote.textContent.substr(1, quote.textContent.length - 2 /* remove quotes */);

        const absHyperlink = text.startsWith("http://") || text.startsWith("https://");
        const relHyperlink = text.match(logicalReferenceRegex);
        const uuid = text.match(uuidRegex);
        const dateTime = text.match(dateTimeRegex);

        if (absHyperlink || relHyperlink) {
            const link = document.createElement("a");
            const href = absHyperlink ? text : "https://hapi.fhir.org/baseR4/" + text;
            link.setAttribute("href", href);
            link.textContent = '"' + text + '"';
            quote.textContent = "";
            quote.appendChild(link);
        }

        if (uuid || dateTime) {
            const span = document.createElement("span");
            span.setAttribute("class", uuid ? "uuid" : "dateTime");
            span.textContent = text;
            quote.textContent = "";
            quote.appendChild(document.createTextNode('"'));
            quote.appendChild(span);
            quote.appendChild(document.createTextNode('"'));
        }
    }
}

(function() {
    'use strict';

    /* bail out if user is testing a version of this script via Greasemonkey or Tampermonkey */
    if (window.HAPI_ResponseHighlighter_userscript) {
        console.log("HAPI ResponseHighlighter: userscript detected - not executing embedded script");
        return;
    }

    console.time("updateHighlightedLine");
    updateHighlightedLine();
    console.timeEnd("updateHighlightedLine");
    window.onhashchange = updateHighlightedLine;

    console.time("updateHyperlinksAndStyles");
    updateHyperlinksAndStyles();
    console.timeEnd("updateHyperlinksAndStyles");

    window.addEventListener("load", function() {
      // https://developer.mozilla.org/en-US/docs/Web/API/Navigation_timing_API
       const now = new Date().getTime();
       const page_load_time = now - performance.timing.navigationStart;
       console.log("User-perceived page loading time: " + page_load_time + "ms");
    });

})();
</script>
<div class="sizeInfo">Wrote 0.4 KB (12.0 KB total including HTML) in approximately 1ms</div></body></html>
```

---
