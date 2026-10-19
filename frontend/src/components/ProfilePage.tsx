import { FormEvent, useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';

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
  onBack: () => void;
};

export default function ProfilePage({ userId, editable, onBack }: ProfilePageProps) {
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
  const [message, setMessage] = useState('Loading profile...');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const url = userId
      ? `${getEnv().API_BASE_URL}/profiles/${encodeURIComponent(userId)}`
      : `${getEnv().API_BASE_URL}/profiles/me`;
    axios
      .get<Profile>(url, { withCredentials: true })
      .then((response) => {
        setProfile(response.data);
        setForm(response.data);
        setMessage('');
      })
      .catch(() => setMessage('Unable to load this profile.'));
  }, [userId]);

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
      setMessage('Profile saved.');
    } catch (error: unknown) {
      if (axios.isAxiosError<{ message?: string }>(error)) {
        setMessage(
          error.response?.data?.message || `Unable to save your profile (${error.response?.status || 'network error'}).`
        );
      } else {
        setMessage('Unable to save your profile.');
      }
    } finally {
      setSaving(false);
    }
  };

  if (!profile) {
    return (
      <section className="profile-panel">
        <button type="button" onClick={onBack}>
          Back to videos
        </button>
        <p role="status">{message}</p>
      </section>
    );
  }

  return (
    <section className="profile-panel" aria-labelledby="profile-title">
      <button type="button" onClick={onBack}>
        Back to videos
      </button>
      <div className="profile-header">
        {profile.avatarPath ? (
          <img className="profile-avatar" src={profile.avatarPath} alt={`${profile.username}'s avatar`} />
        ) : (
          <div className="profile-avatar profile-avatar-placeholder" aria-hidden="true">
            {profile.username.charAt(0).toUpperCase()}
          </div>
        )}
        <div>
          <p className="registration-eyebrow">{editable ? 'MY PROFILE' : 'PROFILE'}</p>
          <h2 id="profile-title">{profile.displayName || profile.username}</h2>
          <p>@{profile.username}</p>
        </div>
      </div>
      {editable ? (
        <form onSubmit={submit} className="profile-form">
          <label htmlFor="display-name">Display name</label>
          <input
            id="display-name"
            value={form.displayName || ''}
            maxLength={100}
            onChange={(event) => setForm({ ...form, displayName: event.target.value })}
          />
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            value={form.description || ''}
            maxLength={2000}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
          />
          <label htmlFor="avatar-path">Avatar URL</label>
          <input
            id="avatar-path"
            type="url"
            value={form.avatarPath || ''}
            onChange={(event) => setForm({ ...form, avatarPath: event.target.value })}
          />
          <label htmlFor="website">Website</label>
          <input
            id="website"
            type="url"
            value={form.website || ''}
            onChange={(event) => setForm({ ...form, website: event.target.value })}
          />
          <label htmlFor="location">Location</label>
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
            Public profile
          </label>
          {message && <p role="status">{message}</p>}
          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save profile'}
          </button>
        </form>
      ) : (
        <div className="profile-details">
          <p>{profile.description || 'This user has not added a description yet.'}</p>
          {profile.location && (
            <p>
              <strong>Location:</strong> {profile.location}
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
