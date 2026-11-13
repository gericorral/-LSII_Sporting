import { FormEvent, useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';
import { useTranslation } from 'react-i18next';

const MAX_DISPLAY_NAME_LENGTH = 50;

export type Profile = {
  userId: string;
  username: string;
  displayName: string | null;
  description: string | null;
  avatarPath: string | null;
  website: string | null;
  location: string | null;
  publicProfile: boolean;
};

type ProfilePageProps = {
  userId?: string;
  editable: boolean;
  onProfileUpdated?: (displayName: string | null, avatarPath: string | null) => void;
  onBack: () => void;
};

export default function ProfilePage({ userId, editable, onProfileUpdated, onBack }: ProfilePageProps) {
  const { t } = useTranslation();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [form, setForm] = useState<Profile>({
    userId: '',
    username: '',
    displayName: '',
    description: '',
    avatarPath: '',
    website: '',
    location: '',
    publicProfile: true,
  });
  const [message, setMessage] = useState(t('profile.loading'));
  const [saving, setSaving] = useState(false);
  const [subscribed, setSubscribed] = useState(false);
  const [subscriptionSaving, setSubscriptionSaving] = useState(false);

  useEffect(() => {
    const url = userId
      ? `${getEnv().API_BASE_URL}/profiles/${encodeURIComponent(userId)}`
      : `${getEnv().API_BASE_URL}/profiles/me`;
    axios
      .get<Profile>(url, { withCredentials: true })
      .then((response) => {
        setProfile(response.data);
        setForm(response.data);
        if (editable) {
          onProfileUpdated?.(response.data.displayName, response.data.avatarPath);
        }
        setMessage('');
      })
      .catch(() => setMessage(t('profile.loadError')));
    if (userId) {
      axios
        .get<{ subscribed: boolean }>(`${getEnv().API_BASE_URL}/profiles/${encodeURIComponent(userId)}/subscription`, {
          withCredentials: true,
        })
        .then((response) => setSubscribed(response.data.subscribed))
        .catch(() => undefined);
    }
  }, [editable, onProfileUpdated, t, userId]);

  const changeSubscription = async () => {
    if (!userId) return;
    setSubscriptionSaving(true);
    try {
      const response = await axios({
        method: subscribed ? 'delete' : 'put',
        url: `${getEnv().API_BASE_URL}/profiles/${encodeURIComponent(userId)}/subscription`,
        withCredentials: true,
      });
      setSubscribed(response.data.subscribed);
    } catch (error: unknown) {
      if (axios.isAxiosError<{ message?: string }>(error) && error.response?.data?.message) {
        setMessage(error.response.data.message);
      } else {
        setMessage(t('profile.updateSubscriptionError'));
      }
    } finally {
      setSubscriptionSaving(false);
    }
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    try {
      const response = await axios.put<Profile>(
        `${getEnv().API_BASE_URL}/profiles/me`,
        {
          displayName: form.displayName || null,
          description: form.description || null,
          avatarPath: form.avatarPath || null,
          website: form.website || null,
          location: form.location || null,
          publicProfile: form.publicProfile,
        },
        { withCredentials: true }
      );
      setProfile(response.data);
      setForm(response.data);
      onProfileUpdated?.(response.data.displayName, response.data.avatarPath);
      setMessage(t('profile.saved'));
    } catch (error: unknown) {
      if (axios.isAxiosError<{ message?: string }>(error)) {
        setMessage(
          error.response?.data?.message ||
            t('profile.saveErrorStatus', { status: error.response?.status || 'network error' })
        );
      } else {
        setMessage(t('profile.saveError'));
      }
    } finally {
      setSaving(false);
    }
  };

  if (!profile) {
    return (
      <section className="profile-panel">
        <button type="button" onClick={onBack}>
          {t('backToVideos')}
        </button>
        <p role="status" aria-live="polite">
          {message}
        </p>
      </section>
    );
  }

  return (
    <section className="profile-panel" aria-labelledby="profile-title">
      <button type="button" onClick={onBack}>
        {t('backToVideos')}
      </button>
      <div className="profile-header">
        {profile.avatarPath ? (
          <img
            className="profile-avatar"
            src={profile.avatarPath}
            alt={t('avatarFor', { username: profile.username })}
          />
        ) : (
          <div className="profile-avatar profile-avatar-placeholder" aria-hidden="true">
            {profile.username.charAt(0).toUpperCase()}
          </div>
        )}
        <div>
          <p className="registration-eyebrow">{editable ? t('profile.myTitle') : t('profile.title')}</p>
          <h2 id="profile-title">{profile.displayName || profile.username}</h2>
          <p>@{profile.username}</p>
        </div>
      </div>
      {editable ? (
        <form onSubmit={submit} className="profile-form">
          <label htmlFor="display-name">{t('profile.displayName')}</label>
          <input
            id="display-name"
            value={form.displayName || ''}
            maxLength={MAX_DISPLAY_NAME_LENGTH}
            onChange={(event) => setForm({ ...form, displayName: event.target.value })}
          />
          <label htmlFor="description">{t('profile.description')}</label>
          <textarea
            id="description"
            value={form.description || ''}
            maxLength={2000}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
          />
          <label htmlFor="avatar-path">{t('profile.avatarUrl')}</label>
          <input
            id="avatar-path"
            type="url"
            value={form.avatarPath || ''}
            onChange={(event) => setForm({ ...form, avatarPath: event.target.value })}
          />
          <label htmlFor="website">{t('profile.website')}</label>
          <input
            id="website"
            type="url"
            value={form.website || ''}
            onChange={(event) => setForm({ ...form, website: event.target.value })}
          />
          <label htmlFor="location">{t('profile.location')}</label>
          <input
            id="location"
            value={form.location || ''}
            maxLength={200}
            onChange={(event) => setForm({ ...form, location: event.target.value })}
          />
          <label className="profile-visibility">
            <input
              type="checkbox"
              checked={form.publicProfile}
              onChange={(event) => setForm({ ...form, publicProfile: event.target.checked })}
            />
            {t('profile.publicProfile')}
          </label>
          {message && (
            <p role="status" aria-live="polite">
              {message}
            </p>
          )}
          <button type="submit" disabled={saving}>
            {saving ? t('profile.saving') : t('profile.save')}
          </button>
        </form>
      ) : (
        <div className="profile-details">
          {userId && (
            <button type="button" onClick={changeSubscription} disabled={subscriptionSaving}>
              {subscriptionSaving
                ? t('profile.updating')
                : subscribed
                  ? t('profile.unsubscribe')
                  : t('profile.subscribe')}
            </button>
          )}
          {message && (
            <p role="status" aria-live="polite">
              {message}
            </p>
          )}
          <p>{profile.description || t('profile.noDescription')}</p>
          {profile.location && (
            <p>
              <strong>{t('profile.location')}:</strong> {profile.location}
            </p>
          )}
          {profile.website && (
            <p>
              <a href={profile.website}>{profile.website}</a>
            </p>
          )}
        </div>
      )}
    </section>
  );
}
