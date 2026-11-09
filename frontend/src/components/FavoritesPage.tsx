import { useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';
import type { VideoSummary } from '../useAllVideos';
import VideoGrid from './VideoGrid';

type FavoritesPageProps = {
  onBack: () => void;
};

const FavoritesPage = ({ onBack }: FavoritesPageProps) => {
  const [videos, setVideos] = useState<VideoSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    axios
      .get<VideoSummary[]>(`${getEnv().API_BASE_URL}/favorites`, { withCredentials: true })
      .then((response) => setVideos(response.data))
      .catch(() => setMessage('Unable to load favorite videos.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="favorites-page">
      <button className="back-button" type="button" onClick={onBack}>
        &larr; Back to videos
      </button>
      <h2>Favorite videos</h2>
      {loading && <p>Loading favorite videos...</p>}
      {message && <p role="alert">{message}</p>}
      {!loading && !message && videos.length === 0 && <p role="status">You have no favorite videos yet.</p>}
      {!loading && !message && videos.length > 0 && <VideoGrid videos={videos} />}
    </section>
  );
};

export default FavoritesPage;
