import { FormEvent, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';
import { useTranslation } from 'react-i18next';

type LoginUser = { id?: string; username: string; email: string };
type LoginFormProps = { onLoggedIn: (user: LoginUser) => void; oauthError?: string };
const LOGIN_URL = `${getEnv().API_BASE_URL}/auth/login`;

export default function LoginForm({ onLoggedIn, oauthError }: LoginFormProps) {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      nextErrors.email = t('auth.validEmail');
    }
    if (!password) nextErrors.password = t('auth.requiredPassword');
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
        setServerError(error.response?.data?.message || t('auth.unableLogin'));
      } else {
        setServerError(t('auth.unableLogin'));
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
        <p className="registration-eyebrow">{t('auth.welcomeBack')}</p>
        <h2 id="login-title">{t('auth.login')}</h2>
        <p>{t('auth.loginDescription')}</p>
      </div>
      <form onSubmit={submit} noValidate>
        <div className="registration-field">
          <label htmlFor="login-email">{t('auth.email')}</label>
          <input
            id="login-email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            aria-invalid={Boolean(errors.email)}
            aria-describedby={errors.email ? 'login-email-error' : undefined}
            required
          />
        </div>
        {errors.email && (
          <small id="login-email-error" role="alert">
            {errors.email}
          </small>
        )}
        <div className="registration-field">
          <label htmlFor="login-password">{t('auth.password')}</label>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            aria-invalid={Boolean(errors.password)}
            aria-describedby={errors.password ? 'login-password-error' : undefined}
            required
          />
        </div>
        {errors.password && (
          <small id="login-password-error" role="alert">
            {errors.password}
          </small>
        )}
        {serverError && <p role="alert">{serverError}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? t('auth.loggingIn') : t('auth.login')}
        </button>
      </form>
      <a
        className="google-login-button"
        href={`${getEnv().API_BASE_URL.replace(/\/api$/, '')}/oauth2/authorization/google`}
      >
        <span className="google-login-icon" aria-hidden="true">
          G
        </span>
        {t('auth.continueGoogle')}
      </a>
      {oauthError && <p role="alert">{oauthError}</p>}
      <p className="registration-footer">
        {t('auth.newTo')} <a href="#register">{t('createAccount')}</a>
      </p>
    </section>
  );
}
