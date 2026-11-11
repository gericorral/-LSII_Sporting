import { useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';
import { useTranslation } from 'react-i18next';

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
  const { t } = useTranslation();
  const [profiles, setProfiles] = useState<SubscribedProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    axios
      .get<SubscribedProfile[]>(`${getEnv().API_BASE_URL}/subscriptions`, { withCredentials: true })
      .then((response) => setProfiles(response.data))
      .catch(() => setMessage(t('pages.subscriptionsError')))
      .finally(() => setLoading(false));
  }, [t]);

  return (
    <section className="subscriptions-page">
      <button className="back-button" type="button" onClick={onBack}>
        &larr; {t('backToVideos')}
      </button>
      <h2>{t('pages.subscriptionTitle')}</h2>
      {loading && <p>{t('pages.loadingSubscriptions')}</p>}
      {message && <p role="alert">{message}</p>}
      {!loading && !message && profiles.length === 0 && <p role="status">{t('pages.noSubscriptions')}</p>}
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
