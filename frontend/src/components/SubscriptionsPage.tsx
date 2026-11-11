import { useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';

type SubscribedProfile = {
  userId: string;
  username: string;
  displayName: string | null;
  avatarPath: string | null;
};

type SubscriptionsPageProps = {
  onBack: () => void;
};

const SubscriptionsPage = ({ onBack }: SubscriptionsPageProps) => {
  const [profiles, setProfiles] = useState<SubscribedProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    axios
      .get<SubscribedProfile[]>(`${getEnv().API_BASE_URL}/subscriptions`, { withCredentials: true })
      .then((response) => setProfiles(response.data))
      .catch(() => setMessage('Unable to load your subscriptions.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="subscriptions-page">
      <button className="back-button" type="button" onClick={onBack}>
        &larr; Back to videos
      </button>
      <h2>Subscriptions</h2>
      {loading && <p>Loading subscriptions...</p>}
      {message && <p role="alert">{message}</p>}
      {!loading && !message && profiles.length === 0 && <p role="status">You are not subscribed to any users yet.</p>}
      {!loading && !message && profiles.length > 0 && (
        <ul className="subscriptions-list">
          {profiles.map((profile) => (
            <li key={profile.userId}>
              <a href={`#profile/${encodeURIComponent(profile.userId)}`}>
                {profile.avatarPath ? (
                  <img className="profile-avatar" src={profile.avatarPath} alt={`${profile.username}'s avatar`} />
                ) : (
                  <span className="profile-avatar profile-avatar-placeholder" aria-hidden="true">
                    {profile.username.charAt(0).toUpperCase()}
                  </span>
                )}
                <span>{profile.displayName || profile.username}</span>
              </a>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
};

export default SubscriptionsPage;
