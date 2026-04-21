export interface CompareCell {
  serverId: number;
  present: boolean;
  statusCode: number | null;
  vulnerable: boolean | null;
  classification: string | null;
  reason: string | null;
  severity: string | null;
}

export interface CompareServerColumn {
  serverId: number;
  serverName: string;
  baseUrl: string;
  testRunId: number | null;
  startedAt: string | null;
  vulnerableCount: number;
  resultCount: number;
}

export interface CompareAttackRow {
  scenarioName: string;
  cells: CompareCell[];
}

export interface CompareResponse {
  servers: CompareServerColumn[];
  attacks: CompareAttackRow[];
}
