import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

import VideoGrid from '../VideoGrid';

describe('this component', () => {
  it('renders correctly', async () => {
    const { asFragment } = render(
      <VideoGrid
        videos={[
          {
            id: '1',
            title: 'Video 1 test',
            description: 'A test video',
            duration: 30,
            thumbnailUrl: '/media/1.webp',
            videoUrl: '/media/1.mp4',
          },
          {
            id: '2',
            title: 'Video 2 test',
            description: '',
            duration: null,
            thumbnailUrl: '/media/2.webp',
            videoUrl: '/media/2.mp4',
          },
        ]}
      />
    );
    expect(screen.queryByText('A test video')).not.toBeInTheDocument();
    expect(screen.queryByText('No description available.')).not.toBeInTheDocument();
    expect(asFragment()).toMatchSnapshot();
  });
});
