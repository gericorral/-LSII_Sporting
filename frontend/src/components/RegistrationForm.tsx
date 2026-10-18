import { FormEvent, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';

type RegistrationUser = {
  username: string;
  email: string;
};

type RegistrationFormProps = {
  onRegistered: (user: RegistrationUser) => void;
};

const REGISTER_URL = `${getEnv().API_BASE_URL}/auth/register`;

export default function RegistrationForm({ onRegistered }: RegistrationFormProps) {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      nextErrors.email = 'Enter a valid email address.';
    }
    if (!/^[a-zA-Z0-9_]{3,50}$/.test(username.trim())) {
      nextErrors.username = 'Use 3-50 letters, numbers or underscores.';
    }
    if (password.length < 8) {
      nextErrors.password = 'Password must contain at least 8 characters.';
    }
    return nextErrors;
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const validationErrors = validate();
    setErrors(validationErrors);
    setServerError('');
    if (Object.keys(validationErrors).length > 0) return;

    try {
      setSubmitting(true);
      const response = await axios.post<RegistrationUser>(
        REGISTER_URL,
        { email: email.trim(), username: username.trim(), password },
        { withCredentials: true }
      );
      onRegistered(response.data);
    } catch (error: unknown) {
      if (axios.isAxiosError<{ field?: string; message?: string }>(error)) {
        const data = error.response?.data;
        if (error.response?.status === 409 && data?.field && data.message) {
          setErrors({ [data.field]: data.message });
        } else {
          setServerError(data?.message || 'Registration failed. Please try again.');
        }
      } else {
        setServerError('Registration failed. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="registration-panel" aria-labelledby="registration-title">
      <div className="registration-brand">
        <div className="registration-brand-mark" aria-hidden="true">
          ▶
        </div>
        <span>PROTUBE</span>
      </div>
      <div className="registration-heading">
        <p className="registration-eyebrow">JOIN THE COMMUNITY</p>
        <h2 id="registration-title">Create your account</h2>
        <p>Join ProTube and start watching your favourite videos.</p>
      </div>
      <form onSubmit={submit} noValidate>
        <div className="registration-field">
          <label htmlFor="email">Email address</label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            aria-invalid={Boolean(errors.email)}
          />
        </div>
        {errors.email && <small role="alert">{errors.email}</small>}

        <div className="registration-field">
          <label htmlFor="username">Username</label>
          <input
            id="username"
            autoComplete="username"
            placeholder="Choose a username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            aria-invalid={Boolean(errors.username)}
          />
        </div>
        {errors.username && <small role="alert">{errors.username}</small>}

        <div className="registration-field">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            autoComplete="new-password"
            placeholder="At least 8 characters"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            aria-invalid={Boolean(errors.password)}
          />
        </div>
        {errors.password && <small role="alert">{errors.password}</small>}
        <p className="registration-hint">Use at least 8 characters to keep your account secure.</p>

        {serverError && <p role="alert">{serverError}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Create my account'}
        </button>
      </form>
      <p className="registration-footer">
        Already have an account? <a href="#">Back to videos</a>
      </p>
    </section>
  );
}
