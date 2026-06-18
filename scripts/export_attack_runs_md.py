#!/usr/bin/env python3
"""Export test run results from API JSON files to Markdown."""

import json
import re
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
IN_DIR = ROOT / "docs" / "attack-runs"
OUT_DIR = IN_DIR

SERVERS = [
    ("HAPI-Public.json", "HAPI Public", "http://hapi.fhir.org/baseR4"),
    ("Firely.json", "Firely", "https://server.fire.ly/r4"),
    ("Smarthealthit.json", "Smarthealthit", "https://r4.smarthealthit.org"),
]

SCENARIO_ORDER = [
    "Malformed JSON Request",
    "Metadata Manipulation",
    "Unexpected Payload Injection",
    "Extension Fields Misuse",
    "Contained Resource Smuggling",
    "Encoded Hidden Data",
    "Invalid Credentials Access Test",
    "Open Endpoint Detection",
    "Authenticated Token Isolation",
    "Cross-Patient Access",
    "Observation Bundle / Duplicate Clinical",
    "Unauthorized Write / ID Tampering",
]


def load_run(path: Path) -> dict:
    with path.open(encoding="utf-8-sig") as f:
        return json.load(f)


def sort_results(results: list) -> list:
    order = {n: i for i, n in enumerate(SCENARIO_ORDER)}
    return sorted(results, key=lambda r: order.get(r.get("scenarioName", ""), 999))


def md_fence(text: str, lang: str = "") -> str:
    if not text:
        return "_empty_"
    # Avoid breaking fences
    fence = "```"
    while fence in text:
        fence += "`"
    return f"{fence}{lang}\n{text.rstrip()}\n{fence}"


def result_section(r: dict) -> str:
    lines = [
        f"### {r['scenarioName']}",
        "",
        "| Field | Value |",
        "|-------|-------|",
        f"| **testResultId** | {r['id']} |",
        f"| **HTTP status** | {r['statusCode']} |",
        f"| **Classification** | `{r['classification']}` |",
        f"| **Vulnerable** | {r['vulnerable']} |",
        f"| **Severity** | {r.get('severity', '')} |",
        f"| **Leakage exposure** | `{r.get('leakageExposure', 'NONE')}` |",
        f"| **Attack vectors** | `{r.get('attackVectorIds', '')}` |",
        "",
        "**Reason:**",
        "",
        r.get("reason", ""),
        "",
        "**Request log:**",
        "",
        md_fence(r.get("requestDetails") or "", "http"),
        "",
        "**Response body:**",
        "",
        md_fence(r.get("responseBody") or ""),
        "",
        "---",
        "",
    ]
    return "\n".join(lines)


def server_doc(name: str, base_url: str, run: dict) -> str:
    results = sort_results(run["results"])
    vuln = sum(1 for r in results if r.get("vulnerable"))
    leak_non_none = sum(1 for r in results if r.get("leakageExposure") not in (None, "", "NONE"))

    header = [
        f"# Attack run — {name}",
        "",
        f"- **Base URL:** `{base_url}`",
        f"- **Test run ID:** {run['id']}",
        f"- **Started at:** {run['startedAt']}",
        f"- **Scenarios:** {len(results)}",
        f"- **VULNERABLE count:** {vuln}",
        f"- **Non-NONE leakage:** {leak_non_none}",
        "",
        "## Summary table",
        "",
        "| # | Scenario | Status | Classification | Severity | Leakage | Vulnerable |",
        "|---|----------|--------|----------------|----------|---------|------------|",
    ]
    for i, r in enumerate(results, 1):
        header.append(
            f"| {i} | {r['scenarioName']} | {r['statusCode']} | `{r['classification']}` | "
            f"{r.get('severity', '')} | `{r.get('leakageExposure', 'NONE')}` | {r['vulnerable']} |"
        )
    header.extend(["", "## Detailed results", ""])
    body = "".join(result_section(r) for r in results)
    return "\n".join(header) + "\n" + body


def main() -> None:
    generated = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    all_runs = []

    for filename, name, base_url in SERVERS:
        path = IN_DIR / filename
        run = load_run(path)
        all_runs.append((name, base_url, run))
        md = server_doc(name, base_url, run)
        out = OUT_DIR / filename.replace(".json", ".md")
        out.write_text(md, encoding="utf-8")
        print(f"Wrote {out} ({len(md):,} bytes)")

    # Combined index + cross-server summary
    index_lines = [
        "# Attack runs — full export (3 servers × 12 scenarios)",
        "",
        f"Generated: **{generated}**",
        "",
        "Servers:",
        "",
        "| Server | Base URL | Test run ID | VULNERABLE | Leakage ≠ NONE |",
        "|--------|----------|-------------|------------|----------------|",
    ]
    for name, base_url, run in all_runs:
        results = run["results"]
        vuln = sum(1 for r in results if r.get("vulnerable"))
        leak = sum(1 for r in results if r.get("leakageExposure") not in (None, "", "NONE"))
        index_lines.append(
            f"| [{name}](./{name.replace(' ', '-')}.md) | `{base_url}` | {run['id']} | {vuln}/12 | {leak}/12 |"
        )
    index_lines.extend(
        [
            "",
            "## Per-server reports",
            "",
            "- [HAPI Public](./HAPI-Public.md)",
            "- [Firely](./Firely.md)",
            "- [Smarthealthit](./Smarthealthit.md)",
            "",
            "## Cross-server matrix (classification)",
            "",
            "| Scenario | HAPI Public | Firely | Smarthealthit |",
            "|----------|-------------|--------|---------------|",
        ]
    )
    by_name = {name: {r["scenarioName"]: r for r in run["results"]} for name, _, run in all_runs}
    for scenario in SCENARIO_ORDER:
        cells = []
        for name, _, _ in all_runs:
            r = by_name[name].get(scenario)
            if r:
                cells.append(f"`{r['classification']}` ({r['statusCode']})")
            else:
                cells.append("—")
        index_lines.append(f"| {scenario} | {' | '.join(cells)} |")

    index_lines.extend(
        [
            "",
            "## Cross-server matrix (leakage)",
            "",
            "| Scenario | HAPI Public | Firely | Smarthealthit |",
            "|----------|-------------|--------|---------------|",
        ]
    )
    for scenario in SCENARIO_ORDER:
        cells = []
        for name, _, _ in all_runs:
            r = by_name[name].get(scenario)
            leak = r.get("leakageExposure", "NONE") if r else "—"
            cells.append(f"`{leak}`" if r else "—")
        index_lines.append(f"| {scenario} | {' | '.join(cells)} |")

    (OUT_DIR / "README.md").write_text("\n".join(index_lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUT_DIR / 'README.md'}")

    # Single combined file for Claude / thesis paste
    combined = [
        "# Complete attack run export (all servers)",
        "",
        f"Generated: **{generated}**",
        "",
    ]
    for name, base_url, run in all_runs:
        combined.append(server_doc(name, base_url, run))
        combined.append("\n\n")
    (OUT_DIR / "ALL-SERVERS-FULL.md").write_text("".join(combined), encoding="utf-8")
    print(f"Wrote {OUT_DIR / 'ALL-SERVERS-FULL.md'}")


if __name__ == "__main__":
    main()
