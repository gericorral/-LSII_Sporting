import { useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from './utils/Env';
import { toSafeId } from './utils/safeId';

type FavoriteState = 'loading' | 'ready' | 'saving' | 'error';

export function useVideoFavorite(videoId: string, authenticated: boolean) {
  const [favorited, setFavorited] = useState(false);
  const [state, setState] = useState<FavoriteState>(authenticated ? 'loading' : 'ready');
  const [message, setMessage] = useState('');

  useEffect(() => {
    let active = true;
    if (!authenticated) {
      setFavorited(false);
      setState('ready');
      setMessage('');
      return () => {
        active = false;
      };
    }
    const safeVideoId = toSafeId(videoId);
    if (!safeVideoId) {
      setState('error');
      setMessage('Unable to load favorite status.');
      return () => {
        active = false;
      };
    }
    setState('loading');
    axios
      .get<{ favorited: boolean }>(`${getEnv().API_BASE_URL}/videos/${safeVideoId}/favorite`, {
        withCredentials: true,
      })
      .then((response) => {
        if (!active) return;
        setFavorited(response.data.favorited);
        setState('ready');
      })
      .catch(() => {
        if (!active) return;
        setState('error');
        setMessage('Unable to load favorite status.');
      });
    return () => {
      active = false;
    };
  }, [authenticated, videoId]);

  const toggleFavorite = async () => {
    const safeVideoId = toSafeId(videoId);
    if (!safeVideoId) {
      setState('error');
      setMessage('The favorite could not be updated.');
      return;
    }
    setMessage('');
    setState('saving');
    try {
      const response = await (favorited
        ? axios.delete<{ favorited: boolean }>(`${getEnv().API_BASE_URL}/videos/${safeVideoId}/favorite`, {
            withCredentials: true,
          })
        : axios.put<{ favorited: boolean }>(
            `${getEnv().API_BASE_URL}/videos/${safeVideoId}/favorite`,
            {},
            { withCredentials: true }
          ));
      setFavorited(response.data.favorited);
      setState('ready');
    } catch {
      setState('error');
      setMessage('The favorite could not be updated.');
    }
  };

  return { favorited, state, message, toggleFavorite };
}
