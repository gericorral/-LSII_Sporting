import { fireEvent, render, screen } from '@testing-library/react';
import App from '../App';
import { useAllVideos } from '../useAllVideos';
import { useVideo } from '../useVideo';
import axios from 'axios';

jest.mock('../useAllVideos');
jest.mock('../useVideo');
jest.mock('../components/LoginForm', () => ({
  __esModule: true,
  default: ({ onLoggedIn }: { onLoggedIn: (user: { username: string; email: string }) => void }) => (
    <button onClick={() => onLoggedIn({ username: 'player_1', email: 'user@example.com' })}>Fake login</button>
  ),
}));
jest.mock('axios');

const mockedUseAllVideos = useAllVideos as jest.MockedFunction<typeof useAllVideos>;
const mockedUseVideo = useVideo as jest.MockedFunction<typeof useVideo>;
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('App routes', () => {
  beforeEach(() => {
    window.location.hash = '';
    mockedUseAllVideos.mockReturnValue({
      loading: 'success',
      message: '',
      value: [],
      hasNext: false,
      loadMore: jest.fn(),
    });
    mockedUseVideo.mockReturnValue({ loading: 'loading', message: '', value: null });
  });

  it('opens registration from the hash route', () => {
    window.location.hash = '#register';
    render(<App />);
    expect(screen.getByRole('heading', { name: 'Create your account' })).toBeInTheDocument();
  });

  it('shows the registered user confirmation', async () => {
    mockedAxios.post.mockResolvedValue({
      data: { email: 'user@example.com', username: 'player_1' },
    });
    window.location.hash = '#register';
    render(<App />);

    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'player_1' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create my account' }));

    expect(await screen.findByText('Welcome, player_1!')).toBeInTheDocument();
  });

  it('shows catalog loading and error states', () => {
    mockedUseAllVideos.mockReturnValueOnce({
      loading: 'loading',
      message: '',
      value: [],
      hasNext: false,
      loadMore: jest.fn(),
    });
    const { rerender } = render(<App />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();

    mockedUseAllVideos.mockReturnValue({
      loading: 'error',
      message: 'network error',
      value: [],
      hasNext: false,
      loadMore: jest.fn(),
    });
    rerender(<App />);
    expect(screen.getByText('network error')).toBeInTheDocument();
  });

  it('shows the idle catalog state', () => {
    mockedUseAllVideos.mockReturnValue({
      loading: 'idle',
      message: '',
      value: [],
      hasNext: false,
      loadMore: jest.fn(),
    });
    render(<App />);

    expect(screen.getByText('Idle...')).toBeInTheDocument();
  });

  it('offers another catalog page when more videos are available', () => {
    const loadMore = jest.fn();
    mockedUseAllVideos.mockReturnValue({
      loading: 'success',
      message: '',
      value: [],
      hasNext: true,
      loadMore,
    });
    render(<App />);

    fireEvent.click(screen.getByRole('button', { name: 'Load more videos' }));
    expect(loadMore).toHaveBeenCalled();
  });

  it('redirects unauthenticated upload access to login', () => {
    window.location.hash = '#upload';
    render(<App />);

    expect(screen.getByRole('button', { name: 'Fake login' })).toBeInTheDocument();
  });

  it('shows the upload form after authenticating from the upload route', async () => {
    window.location.hash = '#upload';
    render(<App />);

    fireEvent.click(screen.getByRole('button', { name: 'Fake login' }));
    expect(await screen.findByRole('heading', { name: 'Upload a video' })).toBeInTheDocument();
  });

  it('shows analytics after authenticating from the analytics route', async () => {
    mockedAxios.get.mockResolvedValue({ data: [] });
    window.location.hash = '#analytics';
    render(<App />);

    fireEvent.click(screen.getByRole('button', { name: 'Fake login' }));
    expect(await screen.findByRole('heading', { name: 'Video analytics' })).toBeInTheDocument();
    expect(await screen.findByText('You have not uploaded any videos yet.')).toBeInTheDocument();
  });

  it('opens a public profile route', () => {
    mockedAxios.get.mockResolvedValue({ data: {} });
    window.location.hash = '#profile/42';
    render(<App />);

    expect(screen.getByRole('status')).toHaveTextContent('Loading profile...');
    fireEvent.click(screen.getByRole('button', { name: 'Back to videos' }));
  });

  it('returns to the catalog after an upload succeeds', async () => {
    mockedAxios.post.mockResolvedValue({ status: 201 });
    window.location.hash = '#upload';
    render(<App />);
    fireEvent.click(screen.getByRole('button', { name: 'Fake login' }));
    fireEvent.change(await screen.findByLabelText('Title'), { target: { value: 'Video' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Description' } });
    fireEvent.change(screen.getByLabelText('MP4 video'), {
      target: { files: [new File(['video'], 'video.mp4', { type: 'video/mp4' })] },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Upload video' }));

    expect(await screen.findByRole('heading', { name: 'Available videos' })).toBeInTheDocument();
  });

  it('authenticates from the login route and logs out', async () => {
    mockedAxios.post.mockResolvedValue({ data: {} });
    window.location.hash = '#login';
    render(<App />);
    fireEvent.click(screen.getByRole('button', { name: 'Fake login' }));
    window.location.hash = '';
    expect(screen.getByText('Hi, player_1')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Log out' }));
    await screen.findByRole('link', { name: 'Log in' });
    expect(mockedAxios.post).toHaveBeenCalledWith(
      expect.stringContaining('/auth/logout'),
      {},
      { withCredentials: true }
    );
  });

  it('shows video detail states', () => {
    window.location.hash = '#video/1';
    const { rerender } = render(<App />);
    expect(screen.getByText('Loading video...')).toBeInTheDocument();

    mockedUseVideo.mockReturnValue({
      loading: 'error',
      message: 'missing video',
      value: null,
    });
    rerender(<App />);
    expect(screen.getByText('missing video')).toBeInTheDocument();
  });

  it('returns to catalog from a video link', () => {
    window.location.hash = '#video/1';
    mockedUseVideo.mockReturnValue({
      loading: 'success',
      message: '',
      value: {
        id: '1',
        title: 'Video',
        description: 'Description',
        duration: 2,
        thumbnailUrl: '/thumb',
        videoUrl: '/video',
      },
    });
    render(<App />);
    fireEvent.click(screen.getByRole('link', { name: /back to videos/i }));
    expect(screen.getByRole('link', { name: /back to videos/i })).toBeInTheDocument();
  });
});
