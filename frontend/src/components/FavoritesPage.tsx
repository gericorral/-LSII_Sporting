import { useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';
import type { VideoSummary } from '../useAllVideos';
import VideoGrid from './VideoGrid';
import { useTranslation } from 'react-i18next';

type FavoritesPageProps = {
  onBack: () => void;
};

const FavoritesPage = ({ onBack }: FavoritesPageProps) => {
  const { t } = useTranslation();
  const [videos, setVideos] = useState<VideoSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    axios
      .get<VideoSummary[]>(`${getEnv().API_BASE_URL}/favorites`, { withCredentials: true })
      .then((response) => setVideos(response.data))
      .catch(() => setMessage(t('pages.favoritesError')))
      .finally(() => setLoading(false));
  }, [t]);

  return (
    <section className="favorites-page" aria-labelledby="favorites-title">
      <button className="back-button" type="button" onClick={onBack}>
        &larr; {t('backToVideos')}
      </button>
      <h2 id="favorites-title">{t('pages.favoriteTitle')}</h2>
      {loading && <p aria-live="polite">{t('pages.loadingFavorites')}</p>}
      {message && <p role="alert">{message}</p>}
      {!loading && !message && videos.length === 0 && <p role="status">{t('pages.noFavorites')}</p>}
      {!loading && !message && videos.length > 0 && <VideoGrid videos={videos} />}
    </section>
  );
};

export default FavoritesPage;
