import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from './utils/Env';

export type VideoAnalytics = {
  videoId: string;
  title: string;
  views: number;
  comments: number;
  likes: number;
};

type LoadingState = 'loading' | 'success' | 'error';

export function useAnalytics(enabled: boolean) {
  const [value, setValue] = useState<VideoAnalytics[]>([]);
  const [loading, setLoading] = useState<LoadingState>('loading');
  const [message, setMessage] = useState('');

  const refresh = useCallback(async () => {
    if (!enabled) return;
    setLoading('loading');
    try {
      const response = await axios.get<VideoAnalytics[]>(`${getEnv().API_BASE_URL}/analytics/me`, {
        withCredentials: true,
      });
      setValue(response.data);
      setMessage('');
      setLoading('success');
    } catch {
      setMessage('Unable to load your analytics.');
      setLoading('error');
    }
  }, [enabled]);

  useEffect(() => {
    refresh().then();
  }, [refresh]);

  return { value, loading, message, refresh };
}
