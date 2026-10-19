import { useCallback, useEffect, useState } from 'react';
import axios, { AxiosError } from 'axios';
import { getEnv } from './utils/Env';

type LoadingState = 'loading' | 'success' | 'error' | 'idle';

export type VideoSummary = {
  id: string;
  title: string;
  description: string;
  duration: number | null;
  thumbnailUrl: string;
  videoUrl: string;
  ownerId?: string | null;
  tags?: string[];
  category?: string;
};

export type VideoPage = {
  content: VideoSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
};

const ALL_VIDEOS_URL = `${getEnv().API_BASE_URL}/videos`;
export function useAllVideos() {
  const [value, setValue] = useState<VideoSummary[]>([]);
  const [message, setMessage] = useState<string>('Loading...');
  const [loading, setLoading] = useState<LoadingState>('idle');
  const [hasNext, setHasNext] = useState(false);
  const [nextPage, setNextPage] = useState(0);

  const refresh = useCallback(async () => {
    try {
      setLoading('loading');
      const response = await axios.get<VideoPage>(ALL_VIDEOS_URL, { params: { page: 0, size: 12 } });
      if (response.status === 200) {
        setValue(response.data.content);
        setHasNext(response.data.hasNext);
        setNextPage(1);
      }
      setLoading('success');
    } catch (error: unknown) {
      setLoading('error');
      setMessage('Error fetching videos: ' + (error as AxiosError).message);
    }
  }, []);

  useEffect(() => {
    refresh().then();
  }, [refresh]);

  const loadMore = async () => {
    if (!hasNext || loading === 'loading') return;
    try {
      setLoading('loading');
      const response = await axios.get<VideoPage>(ALL_VIDEOS_URL, {
        params: { page: nextPage, size: 12 },
      });
      if (response.status === 200) {
        setValue((current) => [...current, ...response.data.content]);
        setHasNext(response.data.hasNext);
        setNextPage((current) => current + 1);
      }
      setLoading('success');
    } catch (error: unknown) {
      setLoading('error');
      setMessage('Error fetching more videos: ' + (error as AxiosError).message);
    }
  };

  return { value, message, loading, hasNext, loadMore, refresh };
}
