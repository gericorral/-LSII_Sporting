import { useEffect, useState } from 'react';
import axios, { AxiosError } from 'axios';
import { getEnv } from './utils/Env';
import type { VideoSummary } from './useAllVideos';

type LoadingState = 'loading' | 'success' | 'error';

export function useVideo(id: string) {
  const [value, setValue] = useState<VideoSummary | null>(null);
  const [loading, setLoading] = useState<LoadingState>('loading');
  const [message, setMessage] = useState('Loading...');

  useEffect(() => {
    const getVideo = async () => {
      try {
        const response = await axios.get<VideoSummary>(`${getEnv().API_BASE_URL}/videos/${encodeURIComponent(id)}`);
        setValue(response.data);
        setLoading('success');
      } catch (error: unknown) {
        setLoading('error');
        setMessage(
          (error as AxiosError).response?.status === 404
            ? 'Video not found.'
            : `Error fetching video: ${(error as AxiosError).message}`
        );
      }
    };
    getVideo().then();
  }, [id]);

  return { value, loading, message };
}
