import { act, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

import App from '../../App';
import { useAllVideos } from '../../useAllVideos';
import { useVideo } from '../../useVideo';

jest.mock('../../useAllVideos');
jest.mock('../../useVideo');

const mockedUseAllVideos = jest.mocked(useAllVideos);
const mockedUseVideo = jest.mocked(useVideo);

const video = {
  id: '1',
  title: 'Video 1',
  description: 'Description',
  duration: 42,
  thumbnailUrl: '/media/1.webp',
  videoUrl: '/media/1.mp4',
};

describe('App video states', () => {
  afterEach(() => {
    window.location.hash = '';
    jest.clearAllMocks();
  });

  it('renders catalog loading, success and error states', () => {
    mockedUseAllVideos.mockReturnValue({ loading: 'loading', message: 'Loading...', value: [] });
    const { rerender } = render(<App />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();

    mockedUseAllVideos.mockReturnValue({ loading: 'success', message: '', value: [video] });
    rerender(<App />);
    expect(screen.getByRole('heading', { name: 'Available videos' })).toBeInTheDocument();

    mockedUseAllVideos.mockReturnValue({ loading: 'error', message: 'Network error', value: [] });
    rerender(<App />);
    expect(screen.getByText('Network error')).toBeInTheDocument();

    mockedUseAllVideos.mockReturnValue({ loading: 'idle', message: '', value: [] });
    rerender(<App />);
    expect(screen.getByText('Idle...')).toBeInTheDocument();
  });

  it('renders detail loading, success and not-found states', async () => {
    window.location.hash = '#video/1';
    mockedUseAllVideos.mockReturnValue({ loading: 'success', message: '', value: [] });
    mockedUseVideo.mockReturnValue({ loading: 'loading', message: 'Loading video...', value: null });
    const { rerender } = render(<App />);
    expect(screen.getByText('Loading video...')).toBeInTheDocument();

    mockedUseVideo.mockReturnValue({ loading: 'success', message: '', value: video });
    rerender(<App />);
    expect(screen.getByRole('heading', { name: 'Video 1' })).toBeInTheDocument();

    mockedUseVideo.mockReturnValue({ loading: 'error', message: 'Video not found.', value: null });
    rerender(<App />);
    expect(screen.getByText('Video not found.')).toBeInTheDocument();

    await act(async () => {
      window.location.hash = '';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByRole('heading', { name: 'Available videos' })).toBeInTheDocument();
  });
});
