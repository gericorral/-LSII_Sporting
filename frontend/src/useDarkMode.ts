import { useEffect, useState } from 'react';
import axios from 'axios';
import { getEnv } from './utils/Env';

type ThemeState = 'loading' | 'ready' | 'saving';

export function useDarkMode(authenticated: boolean) {
  const [darkMode, setDarkMode] = useState(false);
  const [state, setState] = useState<ThemeState>(authenticated ? 'loading' : 'ready');

  useEffect(() => {
    let active = true;
    if (!authenticated) {
      setState('ready');
      return () => {
        active = false;
      };
    }
    setState('loading');
    const request = axios.get<{ darkMode: boolean }>(`${getEnv().API_BASE_URL}/preferences/me`, {
      withCredentials: true,
    });
    if (!request || typeof request.then !== 'function') {
      setState('ready');
      return () => {
        active = false;
      };
    }
    request
      .then((response) => {
        if (!active) return;
        setDarkMode(response.data.darkMode);
        setState('ready');
      })
      .catch(() => {
        if (active) setState('ready');
      });
    return () => {
      active = false;
    };
  }, [authenticated]);

  const toggleDarkMode = async () => {
    const nextDarkMode = !darkMode;
    setDarkMode(nextDarkMode);
    if (!authenticated) {
      return;
    }
    setState('saving');
    try {
      const response = await axios.put<{ darkMode: boolean }>(
        `${getEnv().API_BASE_URL}/preferences/me`,
        { darkMode: nextDarkMode },
        { withCredentials: true }
      );
      setDarkMode(response.data.darkMode);
    } catch {
      setDarkMode(!nextDarkMode);
    } finally {
      setState('ready');
    }
  };

  return { darkMode, state, toggleDarkMode };
}
