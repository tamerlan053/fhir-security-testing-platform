export interface ApiErrorResponse {
  error?: string;
  errors?: string[];
  message?: string;
}

export function formatApiError(err: { error?: ApiErrorResponse }): string {
  const e = err?.error;
  if (e?.errors?.length) return e.errors.join('; ');
  return e?.error || e?.message || 'Request failed';
}
