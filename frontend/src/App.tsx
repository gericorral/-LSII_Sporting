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

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <img src="/protube-logo-removebg-preview.png" className="App-logo" alt="logo" />
        <ContentApp />
      </header>
    </div>
  );
}

function ContentApp() {
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

  if (registering) {
    return registeredUser ? (
      <section className="registration-panel" aria-live="polite">
        <h2>Welcome, {registeredUser.username}!</h2>
        <p>Your account ({registeredUser.email}) is ready and you are now signed in.</p>
        <a href="#">Continue to videos</a>
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
  onLogout: () => void;
}) {
  const { loading, message, value, hasNext, loadMore, refresh, query, search } = useAllVideos();
  const [searchText, setSearchText] = useState('');
  if (videoId) {
    return <VideoDetailPage id={videoId} loggedUser={loggedUser} onUpdated={refresh} />;
  }

  switch (loading) {
    case 'loading':
      return <div>Loading...</div>;
    case 'error':
      return (
        <div>
          <h3>Error</h3> <p>{message}</p>
        </div>
      );
    case 'success':
      return (
        <>
          <div className="catalog-toolbar">
            <div>
              <h2>{query ? `Search results for "${query}"` : 'Available videos'}</h2>
              <form
                className="video-search"
                onSubmit={(event) => {
                  event.preventDefault();
                  search(searchText);
                }}
              >
                <label htmlFor="video-search-input">Search videos</label>
                <input
                  id="video-search-input"
                  value={searchText}
                  onChange={(event) => setSearchText(event.target.value)}
                  placeholder="Search videos"
                />
                <button type="submit">Search</button>
                {query && (
                  <button
                    type="button"
                    onClick={() => {
                      setSearchText('');
                      search('');
                    }}
                  >
                    Clear
                  </button>
                )}
              </form>
            </div>
            <nav className="catalog-actions">
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
                  <a href="#upload">Upload video</a>
                  <a href="#analytics">Analytics</a>
                  <button type="button" onClick={onLogout}>
                    Log out
                  </button>
                </>
              ) : (
                <>
                  <a href="#login">Log in</a>
                  <a href="#register">Create an account</a>
                </>
              )}
            </nav>
          </div>
          {value.length > 0 ? <VideoGrid videos={value} /> : <p role="status">No videos found.</p>}
          {hasNext && (
            <button className="load-more-button" type="button" onClick={loadMore}>
              Load more videos
            </button>
          )}
        </>
      );
  }
  return <div>Idle...</div>;
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
  const { loading, message, value } = useVideo(id);
  if (loading === 'loading') return <div>Loading video...</div>;
  if (loading === 'error' || value === null) {
    return (
      <div>
        <a href="#">&larr; Back to videos</a>
        <h3>Unable to load video</h3>
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
