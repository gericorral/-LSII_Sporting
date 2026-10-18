import { fireEvent, render, screen } from '@testing-library/react';
import App from '../App';
import { useAllVideos } from '../useAllVideos';
import { useVideo } from '../useVideo';

jest.mock('../useAllVideos');
jest.mock('../useVideo');

const mockedUseAllVideos = useAllVideos as jest.MockedFunction<typeof useAllVideos>;
const mockedUseVideo = useVideo as jest.MockedFunction<typeof useVideo>;

describe('App routes', () => {
  beforeEach(() => {
    window.location.hash = '';
    mockedUseAllVideos.mockReturnValue({ loading: 'success', message: '', value: [] });
    mockedUseVideo.mockReturnValue({ loading: 'loading', message: '', value: null });
  });

  it('opens registration from the hash route', () => {
    window.location.hash = '#register';
    render(<App />);
    expect(screen.getByRole('heading', { name: 'Create your account' })).toBeInTheDocument();
  });

  it('shows catalog loading and error states', () => {
    mockedUseAllVideos.mockReturnValueOnce({ loading: 'loading', message: '', value: [] });
    const { rerender } = render(<App />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();

    mockedUseAllVideos.mockReturnValue({ loading: 'error', message: 'network error', value: [] });
    rerender(<App />);
    expect(screen.getByText('network error')).toBeInTheDocument();
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
