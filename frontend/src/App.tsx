import './App.css';
import { useAllVideos } from './useAllVideos';
import VideoGrid from './components/VideoGrid';
import VideoDetail from './components/VideoDetail';
import { useVideo } from './useVideo';
import { useEffect, useState } from 'react';
import RegistrationForm from './components/RegistrationForm';

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
  const [videoId, setVideoId] = useState<string | null>(() => getVideoId());
  const [registering, setRegistering] = useState(() => window.location.hash === '#register');
  const [registeredUser, setRegisteredUser] = useState<{ username: string; email: string } | null>(null);

  useEffect(() => {
    const onHashChange = () => {
      setVideoId(getVideoId());
      setRegistering(window.location.hash === '#register');
    };
    window.addEventListener('hashchange', onHashChange);
    return () => window.removeEventListener('hashchange', onHashChange);
  }, []);

  if (registering) {
    return registeredUser ? (
      <section className="registration-panel" aria-live="polite">
        <h2>Welcome, {registeredUser.username}!</h2>
        <p>Your account ({registeredUser.email}) is ready and you are now signed in.</p>
        <a href="#">Continue to videos</a>
      </section>
    ) : (
      <RegistrationForm onRegistered={setRegisteredUser} />
    );
  }

  return <VideoCatalog videoId={videoId} />;
}

function VideoCatalog({ videoId }: { videoId: string | null }) {
  const { loading, message, value } = useAllVideos();
  if (videoId) {
    return <VideoDetailPage id={videoId} />;
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
            <h2>Available videos</h2>
            <a href="#register">Create an account</a>
          </div>
          <VideoGrid videos={value} />
        </>
      );
  }
  return <div>Idle...</div>;
}

function VideoDetailPage({ id }: { id: string }) {
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
  return <VideoDetail video={value} />;
}

function getVideoId() {
  const match = window.location.hash.match(/^#video\/(.+)$/);
  return match ? decodeURIComponent(match[1]) : null;
}

export default App;
