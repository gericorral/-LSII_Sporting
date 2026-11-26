import { isValidEmail } from '../validation';

describe('isValidEmail', () => {
  it.each(['a@b.com', 'a.b@c.d.es'])('accepts valid email %s', (email) => {
    expect(isValidEmail(email)).toBe(true);
  });

  it.each(['missing-at.com', 'user@', 'a@b', 'a@.com', 'a@b..com', 'a b@c.com'])(
    'rejects invalid email %s',
    (email) => {
      expect(isValidEmail(email)).toBe(false);
    }
  );

  it('rejects a long malformed address without catastrophic matching', () => {
    expect(isValidEmail(`${'a'.repeat(100_000)}@${'b'.repeat(100_000)}`)).toBe(false);
  });
});
