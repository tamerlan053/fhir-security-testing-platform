# Attack runs — full export (3 servers × 12 scenarios)

Generated: **2026-05-26 11:45 UTC**

Servers:

| Server | Base URL | Test run ID | VULNERABLE | Leakage ≠ NONE |
|--------|----------|-------------|------------|----------------|
| [HAPI Public](./HAPI-Public.md) | `http://hapi.fhir.org/baseR4` | 11 | 2/12 | 1/12 |
| [Firely](./Firely.md) | `https://server.fire.ly/r4` | 13 | 3/12 | 0/12 |
| [Smarthealthit](./Smarthealthit.md) | `https://r4.smarthealthit.org` | 12 | 2/12 | 1/12 |

## Per-server reports

- [HAPI Public](./HAPI-Public.md)
- [Firely](./Firely.md)
- [Smarthealthit](./Smarthealthit.md)

## Cross-server matrix (classification)

| Scenario | HAPI Public | Firely | Smarthealthit |
|----------|-------------|--------|---------------|
| Malformed JSON Request | `SECURE` (400) | `VULNERABLE` (201) | `SECURE` (400) |
| Metadata Manipulation | `SECURE` (412) | `SECURE` (201) | `SECURE` (201) |
| Unexpected Payload Injection | `SECURE` (201) | `SECURE` (400) | `SECURE` (201) |
| Extension Fields Misuse | `VULNERABLE` (201) | `VULNERABLE` (201) | `VULNERABLE` (201) |
| Contained Resource Smuggling | `VULNERABLE` (201) | `SECURE` (400) | `VULNERABLE` (201) |
| Encoded Hidden Data | `SECURE` (412) | `VULNERABLE` (201) | `SECURE` (201) |
| Invalid Credentials Access Test | `INCONCLUSIVE` (200) | `INCONCLUSIVE` (200) | `INCONCLUSIVE` (200) |
| Open Endpoint Detection | `OPEN_POLICY` (200) | `OPEN_POLICY` (200) | `OPEN_POLICY` (200) |
| Authenticated Token Isolation | `INCONCLUSIVE` (0) | `INCONCLUSIVE` (0) | `INCONCLUSIVE` (0) |
| Cross-Patient Access | `OPEN_POLICY` (200) | `OPEN_POLICY` (200) | `OPEN_POLICY` (200) |
| Observation Bundle / Duplicate Clinical | `INCONCLUSIVE` (302) | `SECURE` (400) | `OPEN_POLICY` (200) |
| Unauthorized Write / ID Tampering | `OPEN_POLICY` (200) | `OPEN_POLICY` (200) | `OPEN_POLICY` (200) |

## Cross-server matrix (leakage)

| Scenario | HAPI Public | Firely | Smarthealthit |
|----------|-------------|--------|---------------|
| Malformed JSON Request | `NONE` | `NONE` | `NONE` |
| Metadata Manipulation | `NONE` | `NONE` | `NONE` |
| Unexpected Payload Injection | `NONE` | `NONE` | `NONE` |
| Extension Fields Misuse | `NONE` | `NONE` | `NONE` |
| Contained Resource Smuggling | `NONE` | `NONE` | `NONE` |
| Encoded Hidden Data | `NONE` | `NONE` | `NONE` |
| Invalid Credentials Access Test | `NONE` | `NONE` | `NONE` |
| Open Endpoint Detection | `NONE` | `NONE` | `NONE` |
| Authenticated Token Isolation | `NONE` | `NONE` | `NONE` |
| Cross-Patient Access | `NONE` | `NONE` | `NONE` |
| Observation Bundle / Duplicate Clinical | `NONE` | `NONE` | `NONE` |
| Unauthorized Write / ID Tampering | `VERBOSE_ERROR_BODY` | `NONE` | `VERBOSE_ERROR_BODY` |
