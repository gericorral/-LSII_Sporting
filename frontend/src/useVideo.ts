import { useEffect, useState } from 'react';
import axios, { AxiosError } from 'axios';
import { getEnv } from './utils/Env';
import { toSafeId } from './utils/safeId';
import type { VideoSummary } from './useAllVideos';

type LoadingState = 'loading' | 'success' | 'error';

export function useVideo(id: string) {
  const [value, setValue] = useState<VideoSummary | null>(null);
  const [loading, setLoading] = useState<LoadingState>('loading');
  const [message, setMessage] = useState('Loading...');

  useEffect(() => {
    const safeId = toSafeId(id);
    if (!safeId) {
      setLoading('error');
      setMessage('Video not found.');
      return;
    }
    const getVideo = async () => {
      try {
        const response = await axios.get<VideoSummary>(`${getEnv().API_BASE_URL}/videos/${safeId}`);
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
    void getVideo();
  }, [id]);

  return { value, loading, message };
}
