import { useCallback, useEffect, useState } from 'react';
import axios, { AxiosError } from 'axios';
import { getEnv } from './utils/Env';

export type Comment = {
  id: string;
  videoId: string;
  authorId: string;
  authorName: string;
  content: string;
  createdAt: string | number[] | null;
};

type CommentPage = {
  content: Comment[];
  hasNext: boolean;
};

const pageSize = 20;

export function useComments(videoId: string) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');
  const [hasNext, setHasNext] = useState(false);
  const [nextPage, setNextPage] = useState(0);

  const load = useCallback(
    async (page: number, append: boolean) => {
      try {
        setLoading('loading');
        const response = await axios.get<CommentPage>(
          `${getEnv().API_BASE_URL}/videos/${encodeURIComponent(videoId)}/comments`,
          { params: { page, size: pageSize } }
        );
        if (response.status !== 200) {
          setLoading('success');
          return;
        }
        setComments((current) => (append ? [...current, ...response.data.content] : response.data.content));
        setHasNext(response.data.hasNext);
        setNextPage(page + 1);
        setLoading('success');
      } catch (error: unknown) {
        setLoading('error');
        setMessage('Error fetching comments: ' + (error as AxiosError).message);
      }
    },
    [videoId]
  );

  useEffect(() => {
    load(0, false).then();
  }, [load]);

  const loadMore = () => {
    if (!hasNext || loading === 'loading') return Promise.resolve();
    return load(nextPage, true);
  };

  const addComment = (comment: Comment) => {
    setComments((current) => (current.some((item) => item.id === comment.id) ? current : [comment, ...current]));
  };

  return { comments, loading, message, hasNext, loadMore, addComment };
}
