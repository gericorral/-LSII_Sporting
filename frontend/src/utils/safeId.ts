const SAFE_ID_PATTERN = /^[A-Za-z0-9_-]{1,64}$/;

export function toSafeId(value: unknown): string | null {
  if (typeof value === 'number') {
    return Number.isSafeInteger(value) && value > 0 ? String(value) : null;
  }
  if (typeof value !== 'string' || !SAFE_ID_PATTERN.test(value)) {
    return null;
  }
  return value;
}
