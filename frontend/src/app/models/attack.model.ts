export type AttackClassification =
  | 'SECURE'
  | 'VULNERABLE'
  | 'OPEN_POLICY'
  | 'MISCONFIGURED'
  | 'INCONCLUSIVE';

export interface TestResult {
  id: number;
  scenarioName: string;
  statusCode: number;
  /** True only when classification is VULNERABLE (backward-compatible for counts). */
  vulnerable: boolean;
  classification: AttackClassification;
  reason: string;
  severity: string;
  responseBody: string;
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
