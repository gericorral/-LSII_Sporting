import { toSafeId } from '../safeId';

describe('toSafeId', () => {
  it.each(['user_1', 'video-1', 'a', '550e8400-e29b-41d4-a716-446655440000'])(
    'accepts safe string identifiers',
    (value) => {
      expect(toSafeId(value)).toBe(value);
    }
  );

  it('accepts positive safe integer identifiers as strings', () => {
    expect(toSafeId(42)).toBe('42');
  });

  it.each([null, undefined, '', '../', 'a/b', 'id?x=1', 'a.b', 'a'.repeat(65), 0, -1, 1.5, Number.MAX_SAFE_INTEGER + 1])(
    'rejects unsafe identifier %s',
    (value) => {
      expect(toSafeId(value)).toBeNull();
    }
  );
});
