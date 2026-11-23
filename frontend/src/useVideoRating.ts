import { useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from './utils/Env';

export type VideoRating = {
  videoId: string;
  averageRating: number;
  ratingCount: number;
  userRating: number | null;
};

export function useVideoRating(videoId: string, enabled: boolean) {
  const [rating, setRating] = useState<VideoRating | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setMessage('');
    axios
      .get<VideoRating>(`${getEnv().API_BASE_URL}/videos/${encodeURIComponent(videoId)}/rating`, {
        withCredentials: true,
      })
      .then((response) => {
        if (active) setRating(response.data);
      })
      .catch(() => {
        if (active) setMessage('The video rating could not be loaded.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [videoId]);

  const saveRating = async (value: number) => {
    if (!enabled) {
      setMessage('Log in to rate videos.');
      return;
    }
    setSaving(true);
    setMessage('');
    try {
      const response = await axios.put<VideoRating>(
        `${getEnv().API_BASE_URL}/videos/${encodeURIComponent(videoId)}/rating`,
        { rating: value },
        { withCredentials: true }
      );
      setRating(response.data);
    } catch (error: unknown) {
      if (axios.isAxiosError<{ message?: string }>(error)) {
        setMessage(error.response?.data?.message || 'The video rating could not be saved.');
      } else {
        setMessage('The video rating could not be saved.');
      }
    } finally {
      setSaving(false);
    }
  };

  return { rating, loading, saving, message, saveRating };
}
