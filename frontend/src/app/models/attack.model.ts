export type AttackClassification =
  | 'SECURE'
  | 'VULNERABLE'
  | 'OPEN_POLICY'
  | 'INCONCLUSIVE';

export type LeakageExposure = 'NONE' | 'VERBOSE_ERROR_BODY' | 'IMPLEMENTATION_DETAIL_LEAK';

export interface TestResult {
  id: number;
  scenarioName: string;
  statusCode: number;
  /** True only when classification is VULNERABLE (backward-compatible for counts). */
  vulnerable: boolean;
  classification: AttackClassification;
  reason: string;
  severity: string;
  /** Outbound HTTP request log (method, URL, headers, body). */
  requestDetails: string;
  responseBody: string;
  /** Comma-separated stable probe tags from the backend catalog (Week 10). */
  attackVectorIds: string;
  /** Heuristic response-body leakage tier. */
  leakageExposure: LeakageExposure;
}

export interface TestRun {
  id: number;
  serverId: number;
  serverName: string;
  startedAt: string;
  results: TestResult[];
}

export interface RunResult {
  testRunId: number;
  startedAt: string;
}

export interface TestRunSummary {
  testRunId: number;
  startedAt: string;
}
