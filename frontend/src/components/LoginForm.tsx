import { FormEvent, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';

type LoginUser = { username: string; email: string };
type LoginFormProps = { onLoggedIn: (user: LoginUser) => void };
const LOGIN_URL = `${getEnv().API_BASE_URL}/auth/login`;

export default function LoginForm({ onLoggedIn }: LoginFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      nextErrors.email = 'Enter a valid email address.';
    }
    if (!password) nextErrors.password = 'Enter your password.';
    setErrors(nextErrors);
    setServerError('');
    if (Object.keys(nextErrors).length > 0) return;

    try {
      setSubmitting(true);
      const response = await axios.post<LoginUser>(
        LOGIN_URL,
        { email: email.trim(), password },
        { withCredentials: true }
      );
      onLoggedIn(response.data);
    } catch (error: unknown) {
      if (axios.isAxiosError<{ message?: string }>(error)) {
        setServerError(error.response?.data?.message || 'Unable to log in.');
      } else {
        setServerError('Unable to log in.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="registration-panel" aria-labelledby="login-title">
      <div className="registration-brand">
        <div className="registration-brand-mark" aria-hidden="true">
          ▶
        </div>
        <span>PROTUBE</span>
      </div>
      <div className="registration-heading">
        <p className="registration-eyebrow">WELCOME BACK</p>
        <h2 id="login-title">Log in</h2>
        <p>Use your ProTube account to continue.</p>
      </div>
      <form onSubmit={submit} noValidate>
        <div className="registration-field">
          <label htmlFor="login-email">Email address</label>
          <input
            id="login-email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            aria-invalid={Boolean(errors.email)}
          />
        </div>
        {errors.email && <small role="alert">{errors.email}</small>}
        <div className="registration-field">
          <label htmlFor="login-password">Password</label>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            aria-invalid={Boolean(errors.password)}
          />
        </div>
        {errors.password && <small role="alert">{errors.password}</small>}
        {serverError && <p role="alert">{serverError}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Logging in...' : 'Log in'}
        </button>
      </form>
      <p className="registration-footer">
        New to ProTube? <a href="#register">Create an account</a>
      </p>
    </section>
  );
}
