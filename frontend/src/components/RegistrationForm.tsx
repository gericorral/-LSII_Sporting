import { FormEvent, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';
import { useTranslation } from 'react-i18next';

type RegistrationUser = {
  id: string;
  username: string;
  email: string;
};

type RegistrationFormProps = {
  onRegistered: (user: RegistrationUser) => void;
};

const REGISTER_URL = `${getEnv().API_BASE_URL}/auth/register`;

export default function RegistrationForm({ onRegistered }: RegistrationFormProps) {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = () => {
    const nextErrors: Record<string, string> = {};
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      nextErrors.email = t('auth.validEmail');
    }
    if (!/^[a-zA-Z0-9_]{3,50}$/.test(username.trim())) {
      nextErrors.username = t('auth.usernameRules');
    }
    if (password.length < 8) {
      nextErrors.password = t('auth.passwordRules');
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
          setServerError(data?.message || t('auth.registrationFailed'));
        }
      } else {
        setServerError(t('auth.registrationFailed'));
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
        <p className="registration-eyebrow">{t('auth.join')}</p>
        <h2 id="registration-title">{t('auth.create')}</h2>
        <p>{t('auth.createDescription')}</p>
      </div>
      <form onSubmit={submit} noValidate>
        <div className="registration-field">
          <label htmlFor="email">{t('auth.email')}</label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            placeholder={t('auth.emailPlaceholder')}
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            aria-invalid={Boolean(errors.email)}
          />
        </div>
        {errors.email && <small role="alert">{errors.email}</small>}

        <div className="registration-field">
          <label htmlFor="username">{t('auth.username')}</label>
          <input
            id="username"
            autoComplete="username"
            placeholder={t('auth.usernamePlaceholder')}
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            aria-invalid={Boolean(errors.username)}
          />
        </div>
        {errors.username && <small role="alert">{errors.username}</small>}

        <div className="registration-field">
          <label htmlFor="password">{t('auth.password')}</label>
          <input
            id="password"
            type="password"
            autoComplete="new-password"
            placeholder={t('auth.passwordPlaceholder')}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            aria-invalid={Boolean(errors.password)}
          />
        </div>
        {errors.password && <small role="alert">{errors.password}</small>}
        <p className="registration-hint">{t('auth.passwordHint')}</p>

        {serverError && <p role="alert">{serverError}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? t('auth.creating') : t('auth.createMy')}
        </button>
      </form>
      <a
        className="google-login-button"
        href={`${getEnv().API_BASE_URL.replace(/\/api$/, '')}/oauth2/authorization/google`}
      >
        <span className="google-login-icon" aria-hidden="true">
          G
        </span>
        {t('auth.signUpGoogle')}
      </a>
      <p className="registration-footer">
        {t('auth.alreadyAccount')} <a href="#">{t('backToVideos')}</a>
      </p>
    </section>
  );
}
