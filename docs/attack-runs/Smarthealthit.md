# Attack run — Smarthealthit

- **Base URL:** `https://r4.smarthealthit.org`
- **Test run ID:** 12
- **Started at:** 2026-05-26T13:44:07.439863
- **Scenarios:** 12
- **VULNERABLE count:** 2
- **Non-NONE leakage:** 1

## Summary table

| # | Scenario | Status | Classification | Severity | Leakage | Vulnerable |
|---|----------|--------|----------------|----------|---------|------------|
| 1 | Malformed JSON Request | 400 | `SECURE` | INFO | `NONE` | False |
| 2 | Metadata Manipulation | 201 | `SECURE` | INFO | `NONE` | False |
| 3 | Unexpected Payload Injection | 201 | `SECURE` | INFO | `NONE` | False |
| 4 | Extension Fields Misuse | 201 | `VULNERABLE` | MEDIUM | `NONE` | True |
| 5 | Contained Resource Smuggling | 201 | `VULNERABLE` | MEDIUM | `NONE` | True |
| 6 | Encoded Hidden Data | 201 | `SECURE` | INFO | `NONE` | False |
| 7 | Invalid Credentials Access Test | 200 | `INCONCLUSIVE` | LOW | `NONE` | False |
| 8 | Open Endpoint Detection | 200 | `OPEN_POLICY` | INFO | `NONE` | False |
| 9 | Authenticated Token Isolation | 0 | `INCONCLUSIVE` | LOW | `NONE` | False |
| 10 | Cross-Patient Access | 200 | `OPEN_POLICY` | INFO | `NONE` | False |
| 11 | Observation Bundle / Duplicate Clinical | 200 | `OPEN_POLICY` | INFO | `NONE` | False |
| 12 | Unauthorized Write / ID Tampering | 200 | `OPEN_POLICY` | INFO | `VERBOSE_ERROR_BODY` | False |

## Detailed results

### Malformed JSON Request

| Field | Value |
|-------|-------|
| **testResultId** | 121 |
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
POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{ "resourceType": "Patient",

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{ "resourceType": "Patient", }
```

**Response body:**

```
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

---
### Metadata Manipulation

| Field | Value |
|-------|-------|
| **testResultId** | 122 |
| **HTTP status** | 201 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.meta_semantics,fhir.resourceType,fhir.id_identifier` |

**Reason:**

Server did not persist/echo the invalid meta.versionId (client meta appears ignored/sanitized). | Also: SECURE — Server rejected invalid resourceType as expected. | Also: SECURE — Server did not reflect/persist the null-byte marker (id appears sanitized/rewritten). Assigned id: 4689258.

**Request log:**

```http
POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","meta":{"versionId":123}}

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"FakeResource","id":"1"}

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","id":"visible\u0000hidden-data"}

---

GET https://r4.smarthealthit.org/Patient/4689258
```

**Response body:**

```
{
  "resourceType": "Patient",
  "id": "4689257",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:08.311-04:00"
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
  "id": "4689258",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:08.588-04:00"
  }
}
```

---
### Unexpected Payload Injection

| Field | Value |
|-------|-------|
| **testResultId** | 123 |
| **HTTP status** | 201 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,json.unknown_fields,json.duplicate_keys,json.extra_nested` |

**Reason:**

unknownField/__proto__ injection: marker not present on follow-up GET (server likely ignored/stripped unexpected fields). | Also: SECURE — Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed. | Also: SECURE — _payload/extraNested injection: marker not present on follow-up GET (server likely ignored/stripped unexpected fields).

**Request log:**

```http
POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Test"}],"unknownField":"inj-unknown-4f121d210e","__proto__":{"polluted":"inj-unknown-4f121d210e"}}

---

GET https://r4.smarthealthit.org/Patient/4689259

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","id":"valid-id","id":"duplicate-id"}

---

GET https://r4.smarthealthit.org/Patient/4689260

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Test"}],"_payload":"inj-nested-f0fd1d5b39","extraNested":{"secret":"inj-nested-f0fd1d5b39"}}

---

GET https://r4.smarthealthit.org/Patient/4689261
```

**Response body:**

```
unknownField/__proto__ injection POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "4689259",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:08.834-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}

Follow-up GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}       </style>
	</head>

	<body><p>This result is being rendered in HTML for easy viewing. You may ac…
---
Duplicate key POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "4689260",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:09.084-04:00"
  }
}

Follow-up GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}       </style>
	</head>

	<body><p>This result is being rendered in HTML for easy viewing. You may ac…
---
_payload/extraNested injection POST (HTTP 201):
{
  "resourceType": "Patient",
  "id": "4689261",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:09.332-04:00"
  },
  "name": [
    {
      "family": "Test"
    }
  ]
}

Follow-up GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}       </style>
	</head>

	<body><p>This result is being rendered in HTML for easy viewing. You may ac…
```

---
### Extension Fields Misuse

| Field | Value |
|-------|-------|
| **testResultId** | 124 |
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
POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Probe-covert-ext-282e9b31870f"}],"extension":[{"url":"http://malicious.example/hidden","valueString":"covert-ext-282e9b31870f"}]}

---

GET https://r4.smarthealthit.org/Patient/4689262
```

**Response body:**

```
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "4689262",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:09.581-04:00"
  },
  "extension": [
    {
      "url": "http://malicious.example/hidden",
      "valueString": "covert-ext-282e9b31870f"
    }
  ],
  "name": [
    {
      "family": "Probe-covert-ext-282e9b31870f"
    }
  ]
}

GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}  …
```

---
### Contained Resource Smuggling

| Field | Value |
|-------|-------|
| **testResultId** | 125 |
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
POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"family":"Probe-covert-bin-fa8d117db37f"}],"contained":[{"resourceType":"Binary","id":"covert","contentType":"text/plain","data":"Y292ZXJ0LWJpbi1mYThkMTE3ZGIzN2Y="}]}

---

GET https://r4.smarthealthit.org/Patient/4689263
```

**Response body:**

```
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "4689263",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:09.829-04:00"
  },
  "name": [
    {
      "family": "Probe-covert-bin-fa8d117db37f"
    }
  ]
}

GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}  …
```

---
### Encoded Hidden Data

| Field | Value |
|-------|-------|
| **testResultId** | 126 |
| **HTTP status** | 201 |
| **Classification** | `SECURE` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `http.post.patient,fhir.meta.tag,unicode.normalization_display` |

**Reason:**

Meta.tag marker was not present on follow-up GET (server likely normalized/stripped it).

**Request log:**

```http
POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","meta":{"tag":[{"code":"x","display":"\u0063\u006f\u0076\u0065\u0072\u0074\u002d\u0074\u0061\u0067\u002d\u0033\u0062\u0035\u0035\u0039\u0065\u0036\u0031\u0031\u0038\u0066\u0065"}]}}

---

GET https://r4.smarthealthit.org/Patient/4689264
```

**Response body:**

```
POST /Patient (HTTP 201):
{
  "resourceType": "Patient",
  "id": "4689264",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:10.079-04:00",
    "tag": [
      {
        "code": "x",
        "display": "covert-tag-3b559e6118fe"
      }
    ]
  }
}

GET /Patient/{id} (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}  …
```

---
### Invalid Credentials Access Test

| Field | Value |
|-------|-------|
| **testResultId** | 127 |
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
GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata

---

GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata

---

GET https://r4.smarthealthit.org/Patient?_count=1
Authorization: Basic aW52YWxpZDppbnZhbGlk

---

GET https://r4.smarthealthit.org/Patient?_count=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid_signature_week7

---

GET https://r4.smarthealthit.org/Observation?_count=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ.invalid_signature_week7

---

GET https://r4.smarthealthit.org/Patient?_count=1
Authorization: Bearer

---

GET https://r4.smarthealthit.org/Patient?_count=1
Authorization: Bearer not.a.valid.jwt.week7

---

GET https://r4.smarthealthit.org/Patient?_count=1
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
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}       </style>
	</head>

	<body><p>This result is being rendered in HTML for easy viewing. You may ac…
---
Bearer probes Patient 200 | Observation 200
---
Bearer variant → HTTP 200; Bearer variant → HTTP 200; Bearer variant → HTTP 200;
```

---
### Open Endpoint Detection

| Field | Value |
|-------|-------|
| **testResultId** | 128 |
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
GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata

---

GET https://r4.smarthealthit.org/Patient?_count=1
```

**Response body:**

```
well-known HTTP 404, metadata HTTP 200; GET /Patient?_count=1 → HTTP 200. Sample: <html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padd…
```

---
### Authenticated Token Isolation

| Field | Value |
|-------|-------|
| **testResultId** | 129 |
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
GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata
```

**Response body:**

_empty_

---
### Cross-Patient Access

| Field | Value |
|-------|-------|
| **testResultId** | 130 |
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
GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Attacker-2d393b1f25ca"],"family":"PatientA-2d393b1f25ca"}]}

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-9686750dfcba"],"family":"PatientB-9686750dfcba"}]}

---

GET https://r4.smarthealthit.org/Patient/4689266

---

POST https://r4.smarthealthit.org/Observation
Content-Type: application/json

{"resourceType":"Observation","status":"final","code":{"text":"SecurityTest","coding":[{"system":"http://loinc.org","code":"718-7"}]},"subject":{"reference":"Patient/4689266"},"valueString":"CrossPatientProbe-2c4ac82af545"}

---

GET https://r4.smarthealthit.org/Observation?subject=Patient%2F4689266
```

**Response body:**

```
GET Patient → HTTP 200 | GET Observation?subject → HTTP 200
```

---
### Observation Bundle / Duplicate Clinical

| Field | Value |
|-------|-------|
| **testResultId** | 131 |
| **HTTP status** | 200 |
| **Classification** | `OPEN_POLICY` |
| **Vulnerable** | False |
| **Severity** | INFO |
| **Leakage exposure** | `NONE` |
| **Attack vectors** | `fhir.bundle.transaction,http.post.observation_batch,fhir.observation.duplicate_clinical` |

**Reason:**

All three duplicate Observations accepted in one transaction — common on open sandboxes without advertised OAuth.

**Request log:**

```http
GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["BundleDup-57acab7a0c9e"],"family":"ClinicalSubject-57acab7a0c9e"}]}

---

POST https://r4.smarthealthit.org
Content-Type: application/json

{"resourceType":"Bundle","type":"transaction","entry":[{"fullUrl":"urn:uuid:week11-dup-0-03654810fe0a","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/4689268"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-03654810fe0a"}]},"request":{"method":"POST","url":"Observation"}},{"fullUrl":"urn:uuid:week11-dup-1-03654810fe0a","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/4689268"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-03654810fe0a"}]},"request":{"method":"POST","url":"Observation"}},{"fullUrl":"urn:uuid:week11-dup-2-03654810fe0a","resource":{"resourceType":"Observation","status":"final","code":{"coding":[{"system":"http://loinc.org","code":"718-7"}],"text":"Hemoglobin"},"subject":{"reference":"Patient/4689268"},"valueQuantity":{"value":13.2,"unit":"g/dL","system":"http://unitsofmeasure.org","code":"g/dL"},"note":[{"text":"Week11-dup-03654810fe0a"}]},"request":{"method":"POST","url":"Observation"}}]}
```

**Response body:**

```
POST transaction Bundle (3 duplicate-structure Observations for Patient/4689268) → HTTP 200. Created-like entry responses: 3. {
  "resourceType": "Bundle",
  "id": "fe69ee96-78ab-4f25-8502-6bfb9a75188d",
  "type": "transaction-response",
  "link": [
    {
      "relation": "self",
      "url": "https://r4.smarthealthit.org"
    }
  ],
  "entry": [
    {
      "response": {
        "status": "201 Created",
        "location": "Observation/4689269/_history/1",
        "etag": "1",
        "lastModified": "2026-05-26T07:44:19.942-04:00"
      }
    },
    {
      "response": {
        "status": "201 Created",
        "location": "Observation/4689270/_history/1",
        "etag": "1",
        "lastModified": "2026-05-26T07:44:19.942-04:00"
      }
    },
    {
      "response": {
        "status": "201 Created",
        "location": "Observation/4689271/_history/1",
        "etag": "1",
        "lastModified": "2026-05-26T07:44:19.942-04:00"
      }
    }
  ]
}
```

---
### Unauthorized Write / ID Tampering

| Field | Value |
|-------|-------|
| **testResultId** | 132 |
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
GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-69d452864841"],"family":"PatientVictim-69d452864841"}]}

---

GET https://r4.smarthealthit.org/Patient/4689272

---

PUT https://r4.smarthealthit.org/Patient/4689272
Content-Type: application/json

{"resourceType":"Patient","id":"4689272","name":[{"given":["Tampered-ebfd0c21dfa3"],"family":"TamperedFamily-ebfd0c21dfa3"}]}

---

GET https://r4.smarthealthit.org/Patient/4689272

---

GET https://r4.smarthealthit.org/.well-known/smart-configuration

---

GET https://r4.smarthealthit.org/metadata

---

POST https://r4.smarthealthit.org/Patient
Content-Type: application/json

{"resourceType":"Patient","name":[{"given":["Victim-1484e75b5d50"],"family":"PatientOwnerRef-1484e75b5d50"}]}

---

POST https://r4.smarthealthit.org/Observation
Content-Type: application/json

{"resourceType":"Observation","status":"final","code":{"text":"SecurityTest","coding":[{"system":"http://loinc.org","code":"718-7"}]},"subject":{"reference":"Patient/4689273"},"valueString":"OwnerRefProbe-d196b787d866"}

---

GET https://r4.smarthealthit.org/Observation/4689274
```

**Response body:**

```
GET /Patient/{id} before PUT (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.re…

PUT /Patient/{id} (HTTP 200):
{
  "resourceType": "Patient",
  "id": "4689272",
  "meta": {
    "versionId": "2",
    "lastUpdated": "2026-05-26T07:44:21.505-04:00"
  },
  "name": [
    {
      "family": "TamperedFamily-ebfd0c21dfa3",
      "given": [
        "Tampered-ebfd0c21dfa3"
      ]
    }
  ]
}

GET /Patient/{id} after PUT (HTTP 200):
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padd…

POST /Observation:
{
  "resourceType": "Observation",
  "id": "4689274",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2026-05-26T07:44:22.949-04:00"
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
    "reference": "Patient/4689273"
  },
  "valueString": "OwnerRefProbe-d196b787d866"
}

GET /Observation/{id}:
<html lang="en">
	<head>
		<meta charset="utf-8" />
       <style>
.httpStatusDiv {  font-size: 1.2em;  font-weight: bold;}.hlQuot { color: #88F; }
.hlQuot a { text-decoration: underline; text-decoration-color: #CCC; }
.hlQuot a:HOVER { text-decoration: underline; text-decoration-color: #008; }
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
}.headersDiv {
  padding: 10px;  margin-left: 10px;  border: 1px solid #CCC;  border-radius: 10px;}.headersRow {
}.headerName {
  color: #888;
  font-family: monospace;
}.headerValue {
  color: #88F;
  font-family: monospace;
}.responseBodyTable {  width: 100%;
  margin-left: 0px;
  margin-top: -10px;
  position: relative;
}.responseBodyTableFirstColumn {}.responseBodyTableSecondColumn {  position: absolute;
  margin-left: 70px;
  vertical-align: top;
  left: 0px;
  right: 0px;
}.responseBodyTableSecondColumn PRE {  margin: 0px;}.sizeInfo {  margin-top: 20px;  font-size: 0.8em;}.lineAnchor A {  text-decoration: none;  padding-left: 20px;}.lineAnchor {  display: block;  padding-right: 20px;}.selectedLine {  background-color: #EEF;  font-weight: bold;}H1 {  font-size: 1.1em;  color: #666;}BODY {
  font-family: Arial;
}       </style>
	</head>

	<body><p>This result is being rendered in HTML for easy viewing. You may access this content as <a href="?_format=json">Raw JSON</a> or <a href="?_format=xml">Raw XML</a>,  or view this content in <a href="?_format=html/json">HTML JSON</a> or <a href="?_format=html/xml">HTML XML</a>. Response generated in 3ms.</p>
<div class="httpStatusDiv">HTTP 200 OK</div>

<h1>Response Headers</h1><div class="headersDiv"><div class="headersRow"><span class="headerName">Date: </span><span class="headerValue">Tue, 26 May 2026 11:44:23 GMT</span></div><div class="headersRow"><span class="headerName">X-Powered-By: </span><span class="headerValue">Smile CDR 2019.08.PRE FHIR REST Endpoint (R4) (FHIR Server; FHIR 4.0.0/R4; HAPI FHIR 4.0.0-SNAPSHOT)</span></div><div class="headersRow"><span class="headerName">Content-Type: </span><span class="headerValue">text/html;charset=utf-8</span></div></div><h1>Response Body</h1><div class="responseBodyTable"><div class="responseBodyTableSecondColumn"><pre><div id="line1"><span class='hlControl'>{</span></div><div id="line2" onclick="updateHighlightedLineTo('#L2');">  <span class='hlTagName'>&quot;resourceType&quot;</span>: <span class='hlQuot'>&quot;Observation&quot;</span><span class='hlControl'>,</span></div><div id="line3" onclick="updateHighlightedLineTo('#L3');">  <span class='hlTagName'>&quot;id&quot;</span>: <span class='hlQuot'>&quot;4689274&quot;</span><span class='hlControl'>,</span></div><div id="line4" onclick="updateHighlightedLineTo('#L4');">  <span class='hlTagName'>&quot;meta&quot;</span>: <span class='hlControl'>{</span></div><div id="line5" onclick="updateHighlightedLineTo('#L5');">    <span class='hlTagName'>&quot;versionId&quot;</span>: <span class='hlQuot'>&quot;1&quot;</span><span class='hlControl'>,</span></div><div id="line6" onclick="updateHighlightedLineTo('#L6');">    <span class='hlTagName'>&quot;lastUpdated&quot;</span>: <span class='hlQuot'>&quot;2026-05-26T07:44:22.949-04:00&quot;</span></div><div id="line7" onclick="updateHighlightedLineTo('#L7');">  <span class='hlControl'>}</span><span class='hlControl'>,</span></div><div id="line8" onclick="updateHighlightedLineTo('#L8');">  <span class='hlTagName'>&quot;status&quot;</span>: <span class='hlQuot'>&quot;final&quot;</span><span class='hlControl'>,</span></div><div id="line9" onclick="updateHighlightedLineTo('#L9');">  <span class='hlTagName'>&quot;code&quot;</span>: <span class='hlControl'>{</span></div><div id="line10" onclick="updateHighlightedLineTo('#L10');">    <span class='hlTagName'>&quot;coding&quot;</span>: <span class='hlControl'>[</span></div><div id="line11" onclick="updateHighlightedLineTo('#L11');">      <span class='hlControl'>{</span></div><div id="line12" onclick="updateHighlightedLineTo('#L12');">        <span class='hlTagName'>&quot;system&quot;</span>: <span class='hlQuot'>&quot;http://loinc.org&quot;</span><span class='hlControl'>,</span></div><div id="line13" onclick="updateHighlightedLineTo('#L13');">        <span class='hlTagName'>&quot;code&quot;</span>: <span class='hlQuot'>&quot;718-7&quot;</span></div><div id="line14" onclick="updateHighlightedLineTo('#L14');">      <span class='hlControl'>}</span></div><div id="line15" onclick="updateHighlightedLineTo('#L15');">    ]<span class='hlControl'>,</span></div><div id="line16" onclick="updateHighlightedLineTo('#L16');">    <span class='hlTagName'>&quot;text&quot;</span>: <span class='hlQuot'>&quot;SecurityTest&quot;</span></div><div id="line17" onclick="updateHighlightedLineTo('#L17');">  <span class='hlControl'>}</span><span class='hlControl'>,</span></div><div id="line18" onclick="updateHighlightedLineTo('#L18');">  <span class='hlTagName'>&quot;subject&quot;</span>: <span class='hlControl'>{</span></div><div id="line19" onclick="updateHighlightedLineTo('#L19');">    <span class='hlTagName'>&quot;reference&quot;</span>: <span class='hlQuot'>&quot;Patient/4689273&quot;</span></div><div id="line20" onclick="updateHighlightedLineTo('#L20');">  <span class='hlControl'>}</span><span class='hlControl'>,</span></div><div id="line21" onclick="updateHighlightedLineTo('#L21');">  <span class='hlTagName'>&quot;valueString&quot;</span>: <span class='hlQuot'>&quot;OwnerRefProbe-d196b787d866&quot;</span></div><div id="line22" onclick="updateHighlightedLineTo('#L22');"><span class='hlControl'>}</span></div></pre></div><div class="responseBodyTableFirstColumn"><pre><div class="lineAnchor" id="anchor1"><a href="#L1" name="L1" id="L1">1</a></div><div class="lineAnchor" id="anchor2"><a href="#L2" name="L2" id="L2">2</a></div><div class="lineAnchor" id="anchor3"><a href="#L3" name="L3" id="L3">3</a></div><div class="lineAnchor" id="anchor4"><a href="#L4" name="L4" id="L4">4</a></div><div class="lineAnchor" id="anchor5"><a href="#L5" name="L5" id="L5">5</a></div><div class="lineAnchor" id="anchor6"><a href="#L6" name="L6" id="L6">6</a></div><div class="lineAnchor" id="anchor7"><a href="#L7" name="L7" id="L7">7</a></div><div class="lineAnchor" id="anchor8"><a href="#L8" name="L8" id="L8">8</a></div><div class="lineAnchor" id="anchor9"><a href="#L9" name="L9" id="L9">9</a></div><div class="lineAnchor" id="anchor10"><a href="#L10" name="L10" id="L10">10</a></div><div class="lineAnchor" id="anchor11"><a href="#L11" name="L11" id="L11">11</a></div><div class="lineAnchor" id="anchor12"><a href="#L12" name="L12" id="L12">12</a></div><div class="lineAnchor" id="anchor13"><a href="#L13" name="L13" id="L13">13</a></div><div class="lineAnchor" id="anchor14"><a href="#L14" name="L14" id="L14">14</a></div><div class="lineAnchor" id="anchor15"><a href="#L15" name="L15" id="L15">15</a></div><div class="lineAnchor" id="anchor16"><a href="#L16" name="L16" id="L16">16</a></div><div class="lineAnchor" id="anchor17"><a href="#L17" name="L17" id="L17">17</a></div><div class="lineAnchor" id="anchor18"><a href="#L18" name="L18" id="L18">18</a></div><div class="lineAnchor" id="anchor19"><a href="#L19" name="L19" id="L19">19</a></div><div class="lineAnchor" id="anchor20"><a href="#L20" name="L20" id="L20">20</a></div><div class="lineAnchor" id="anchor21"><a href="#L21" name="L21" id="L21">21</a></div><div class="lineAnchor" id="anchor22"><a href="#L22" name="L22" id="L22">22</a></div></div></td></div>
<script type="text/javascript">
var selectedLines = new Array();
function updateHighlightedLine() {
	updateHighlightedLineTo(window.location.hash);
}

function updateHighlightedLineTo(theNewHash) {
	
	for (var next in selectedLines) {
		document.getElementById('line' + selectedLines[next]).className = '';
		document.getElementById('anchor' + selectedLines[next]).className = 'lineAnchor';
	}
	selectedLines = new Array();
	
	var line = -1;
	if (theNewHash && theNewHash.match('L[0-9]+-L[0-9]+')) {
		var dashIndex = theNewHash.indexOf('-');
		var start = parseInt(theNewHash.substring(2, dashIndex));
		var end = parseInt(theNewHash.substring(dashIndex+2));
		for (var i = start; i <= end; i++) {
			selectedLines.push(i);
		}
	} else if (theNewHash && theNewHash.match('L[0-9]+')) {
		var line = parseInt(theNewHash.substring(2));
		selectedLines.push(line);
	}


	for (var next in selectedLines) {
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
    const dateTimeRegex = /^-?[0-9]{4}(-(0[1-9]|1[0-2])(-(0[0-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\.[0-9]+)?(Z|(\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?$/; // from the spec - https://www.hl7.org/fhir/datatypes.html#datetime
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
            const href = absHyperlink ? text : "https://r4.smarthealthit.org/" + text;
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

    window.addEventListener("load", function(event) {
      // https://developer.mozilla.org/en-US/docs/Web/API/Navigation_timing_API
      var now = new Date().getTime();
      var page_load_time = now - performance.timing.navigationStart;
      console.log("User-perceived page loading time: " + page_load_time + "ms");
    });

})();
</script>
<div class="sizeInfo">Wrote 0.4 KB (11.5 KB total including HTML) in estimated 1ms</div></body></html>
```

---
