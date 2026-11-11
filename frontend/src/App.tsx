import './App.css';
import { useAllVideos } from './useAllVideos';
import VideoGrid from './components/VideoGrid';
import VideoDetail from './components/VideoDetail';
import { useVideo } from './useVideo';
import { useCallback, useEffect, useState } from 'react';
import RegistrationForm from './components/RegistrationForm';
import LoginForm from './components/LoginForm';
import axios from 'axios';
import { getEnv } from './utils/Env';
import ProfilePage from './components/ProfilePage';
import UploadForm from './components/UploadForm';
import AnalyticsPage from './components/AnalyticsPage';
import FavoritesPage from './components/FavoritesPage';
import SubscriptionsPage from './components/SubscriptionsPage';
import { useDarkMode } from './useDarkMode';
import { useTranslation } from 'react-i18next';
import LanguageSelector from './components/LanguageSelector';

function App() {
  const [authenticated, setAuthenticated] = useState(false);
  const { darkMode, state: themeState, toggleDarkMode } = useDarkMode(authenticated);

  return (
    <div className={`App ${darkMode ? 'dark-mode' : 'light-mode'}`}>
      <header className="App-header">
        <img src="/protube-logo-removebg-preview.png" className="App-logo" alt="logo" />
        <LanguageSelector />
        <ContentApp
          onAuthenticationChange={setAuthenticated}
          darkMode={darkMode}
          themeState={themeState}
          onToggleDarkMode={toggleDarkMode}
        />
      </header>
    </div>
  );
}

function ContentApp({
  onAuthenticationChange,
  darkMode,
  themeState,
  onToggleDarkMode,
}: {
  onAuthenticationChange: (authenticated: boolean) => void;
  darkMode: boolean;
  themeState: 'loading' | 'ready' | 'saving';
  onToggleDarkMode: () => Promise<void>;
}) {
  const { t } = useTranslation();
  const [hash, setHash] = useState(() => window.location.hash);
  const [registeredUser, setRegisteredUser] = useState<{
    id?: string;
    username: string;
    email: string;
    displayName?: string | null;
    avatarPath?: string | null;
  } | null>(null);
  const [loggedUser, setLoggedUser] = useState<{
    id?: string;
    username: string;
    email: string;
    displayName?: string | null;
    avatarPath?: string | null;
  } | null>(null);
  const authenticatedUser = loggedUser || registeredUser;
  useEffect(() => {
    onAuthenticationChange(Boolean(authenticatedUser));
  }, [authenticatedUser, onAuthenticationChange]);

  useEffect(() => {
    const onHashChange = () => setHash(window.location.hash);
    window.addEventListener('hashchange', onHashChange);
    return () => window.removeEventListener('hashchange', onHashChange);
  }, []);

  useEffect(() => {
    if (typeof axios.get !== 'function') return;
    const request = axios.get(`${getEnv().API_BASE_URL}/auth/me`, { withCredentials: true });
    if (request && typeof request.then === 'function') {
      request.then((response) => setLoggedUser(response.data)).catch(() => undefined);
    }
  }, []);

  const videoId = getVideoId(hash);
  const registering = hash === '#register';
  const loggingIn = hash === '#login' || hash.startsWith('#login?');
  const oauthError = hash.startsWith('#login?oauthError=')
    ? decodeURIComponent(hash.substring('#login?oauthError='.length))
    : undefined;
  const profileId = getProfileId(hash);
  const uploading = hash === '#upload';
  const analytics = hash === '#analytics';
  const favorites = hash === '#favorites';
  const subscriptions = hash === '#subscriptions';
  const handleProfileUpdated = useCallback(
    (displayName: string | null | undefined, avatarPath: string | null | undefined) => {
      setLoggedUser((user) => (user ? { ...user, displayName, avatarPath } : user));
      setRegisteredUser((user) => (user ? { ...user, displayName, avatarPath } : user));
    },
    []
  );

  if (loggingIn) {
    return (
      <LoginForm
        oauthError={oauthError}
        onLoggedIn={(user) => {
          setLoggedUser(user);
          setHash('');
          window.location.hash = '';
        }}
      />
    );
  }

  if (hash === '#profile' || profileId) {
    return (
      <ProfilePage
        userId={profileId}
        editable={!profileId}
        onProfileUpdated={handleProfileUpdated}
        onBack={() => {
          setHash('');
          window.location.hash = '';
        }}
      />
    );
  }

  if (uploading) {
    return authenticatedUser ? (
      <UploadForm
        onUploaded={() => {
          setHash('');
          window.location.hash = '';
        }}
      />
    ) : (
      <LoginForm
        onLoggedIn={(user) => {
          setLoggedUser(user);
          window.location.hash = '#upload';
        }}
      />
    );
  }

  if (analytics) {
    return authenticatedUser ? (
      <AnalyticsPage
        onBack={() => {
          setHash('');
          window.location.hash = '';
        }}
      />
    ) : (
      <LoginForm
        onLoggedIn={(user) => {
          setLoggedUser(user);
          window.location.hash = '#analytics';
        }}
      />
    );
  }

  if (favorites) {
    return authenticatedUser ? (
      <FavoritesPage
        onBack={() => {
          setHash('');
          window.location.hash = '';
        }}
      />
    ) : (
      <LoginForm
        onLoggedIn={(user) => {
          setLoggedUser(user);
          window.location.hash = '#favorites';
        }}
      />
    );
  }

  if (subscriptions) {
    return authenticatedUser ? (
      <SubscriptionsPage
        onBack={() => {
          setHash('');
          window.location.hash = '';
        }}
      />
    ) : (
      <LoginForm
        onLoggedIn={(user) => {
          setLoggedUser(user);
          window.location.hash = '#subscriptions';
        }}
      />
    );
  }

  if (registering) {
    return registeredUser ? (
      <section className="registration-panel" aria-live="polite">
        <h2>{t('welcomeUser', { username: registeredUser.username })}</h2>
        <p>{t('accountReady', { email: registeredUser.email })}</p>
        <a href="#">{t('continueVideos')}</a>
      </section>
    ) : (
      <RegistrationForm
        onRegistered={(user) => {
          setRegisteredUser(user);
          setLoggedUser(user);
        }}
      />
    );
  }

  return (
    <VideoCatalog
      videoId={videoId}
      loggedUser={authenticatedUser}
      darkMode={darkMode}
      themeState={themeState}
      onToggleDarkMode={onToggleDarkMode}
      onLogout={() => {
        axios.post(`${getEnv().API_BASE_URL}/auth/logout`, {}, { withCredentials: true }).finally(() => {
          setLoggedUser(null);
          setRegisteredUser(null);
        });
      }}
    />
  );
}

function VideoCatalog({
  videoId,
  loggedUser,
  darkMode,
  themeState,
  onToggleDarkMode,
  onLogout,
}: {
  videoId: string | null;
  loggedUser: {
    id?: string;
    username: string;
    email: string;
    displayName?: string | null;
    avatarPath?: string | null;
  } | null;
  darkMode: boolean;
  themeState: 'loading' | 'ready' | 'saving';
  onToggleDarkMode: () => Promise<void>;
  onLogout: () => void;
}) {
  const { t } = useTranslation();
  const { loading, message, value, hasNext, loadMore, refresh, query, search } = useAllVideos();
  const [searchText, setSearchText] = useState('');
  if (videoId) {
    return <VideoDetailPage id={videoId} loggedUser={loggedUser} onUpdated={refresh} />;
  }

  switch (loading) {
    case 'loading':
      return <div>{t('loading')}</div>;
    case 'error':
      return (
        <div>
          <h3>{t('error')}</h3> <p>{message}</p>
        </div>
      );
    case 'success':
      return (
        <>
          <div className="catalog-toolbar">
            <div>
              <h2>{query ? t('searchResultsFor', { query }) : t('availableVideos')}</h2>
              <form
                className="video-search"
                onSubmit={(event) => {
                  event.preventDefault();
                  search(searchText);
                }}
              >
                <label htmlFor="video-search-input">{t('searchVideos')}</label>
                <input
                  id="video-search-input"
                  value={searchText}
                  onChange={(event) => setSearchText(event.target.value)}
                  placeholder={t('searchVideos')}
                />
                <button type="submit">{t('search')}</button>
                {query && (
                  <button
                    type="button"
                    onClick={() => {
                      setSearchText('');
                      search('');
                    }}
                  >
                    {t('clear')}
                  </button>
                )}
              </form>
            </div>
            <nav className="catalog-actions">
              <button
                type="button"
                className="theme-toggle"
                onClick={onToggleDarkMode}
                disabled={themeState !== 'ready'}
                aria-label={darkMode ? t('lightMode') : t('darkMode')}
                aria-pressed={darkMode}
              >
                {darkMode ? t('lightMode') : t('darkMode')}
              </button>
              {loggedUser ? (
                <>
                  <a className="catalog-profile-link" href="#profile">
                    {loggedUser.avatarPath ? (
                      <img className="catalog-profile-avatar" src={loggedUser.avatarPath} alt="Profile avatar" />
                    ) : (
                      <span className="catalog-profile-avatar catalog-profile-avatar-placeholder" aria-hidden="true">
                        {(loggedUser.displayName || loggedUser.username || loggedUser.email || '?')
                          .charAt(0)
                          .toUpperCase()}
                      </span>
                    )}
                    <span>Hi, {loggedUser.displayName || loggedUser.username}</span>
                  </a>
                  <a href="#upload">{t('uploadVideo')}</a>
                  <a href="#analytics">{t('analytics')}</a>
                  <a href="#favorites">{t('favorites')}</a>
                  <a href="#subscriptions">{t('subscriptions')}</a>
                  <button type="button" onClick={onLogout}>
                    {t('logout')}
                  </button>
                </>
              ) : (
                <>
                  <a href="#login">{t('login')}</a>
                  <a href="#register">{t('createAccount')}</a>
                </>
              )}
            </nav>
          </div>
          {value.length > 0 ? <VideoGrid videos={value} /> : <p role="status">{t('noVideosFound')}</p>}
          {hasNext && (
            <button className="load-more-button" type="button" onClick={loadMore}>
              {t('loadMoreVideos')}
            </button>
          )}
        </>
      );
  }
  return <div>{t('idle')}</div>;
}

function VideoDetailPage({
  id,
  loggedUser,
  onUpdated,
}: {
  id: string;
  loggedUser: {
    id?: string;
    username: string;
    email: string;
    displayName?: string | null;
    avatarPath?: string | null;
  } | null;
  onUpdated: () => Promise<void>;
}) {
  const { t } = useTranslation();
  const { loading, message, value } = useVideo(id);
  if (loading === 'loading') return <div>{t('loadingVideo')}</div>;
  if (loading === 'error' || value === null) {
    return (
      <div>
        <a href="#">&larr; {t('backToVideos')}</a>
        <h3>{t('video.unableLoad')}</h3>
        <p>{message}</p>
      </div>
    );
  }
  return (
    <VideoDetail
      video={value}
      canEdit={Boolean(loggedUser?.id && loggedUser.id === value.ownerId)}
      onUpdated={onUpdated}
      loggedUser={loggedUser}
    />
  );
}

function getVideoId(hash: string) {
  const match = hash.match(/^#video\/(.+)$/);
  return match ? decodeURIComponent(match[1]) : null;
}

function getProfileId(hash: string) {
  const match = hash.match(/^#profile\/(.+)$/);
  return match ? decodeURIComponent(match[1]) : undefined;
}

export default App;
