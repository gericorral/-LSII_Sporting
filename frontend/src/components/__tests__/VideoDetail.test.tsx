import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

import VideoDetail from '../VideoDetail';

describe('VideoDetail', () => {
  it('renders the player and video metadata', () => {
    render(
      <VideoDetail
        video={{
          id: '1',
          title: 'Video 1',
          description: 'A description',
          duration: 42,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
        }}
      />,
    );

    expect(screen.getByRole('heading', { name: 'Video 1' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /back to videos/i })).toHaveAttribute(
      'href',
      '#',
    );
    expect(screen.getByText('A description')).toBeInTheDocument();
    expect(document.querySelector('video')).toHaveAttribute('src', '/media/1.mp4');
  });

  it('renders without duration when it is unavailable', () => {
    render(
      <VideoDetail
        video={{
          id: '2',
          title: 'Video 2',
          description: '',
          duration: null,
          thumbnailUrl: '/media/2.webp',
          videoUrl: '/media/2.mp4',
        }}
      />,
    );

    expect(document.querySelector('.video-detail span')).not.toBeInTheDocument();
    expect(screen.getByText('Video 2')).toBeInTheDocument();
  });
});
