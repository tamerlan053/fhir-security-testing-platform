export interface TestResult {
    id: number;
    scenarioName: string;
    statusCode: number;
    vulnerable: boolean;
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
