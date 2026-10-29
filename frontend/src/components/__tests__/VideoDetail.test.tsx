import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';

import VideoDetail from '../VideoDetail';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('VideoDetail', () => {
  beforeEach(() => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { content: [], hasNext: false },
    });
    mockedAxios.put.mockReset();
    mockedAxios.post.mockReset();
  });

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
      />
    );

    expect(screen.getByRole('heading', { name: 'Video 1' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /back to videos/i })).toHaveAttribute('href', '#');
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
      />
    );

    expect(document.querySelector('.video-detail span')).not.toBeInTheDocument();
    expect(screen.getByText('Video 2')).toBeInTheDocument();
  });

  it('registers one view when playback starts', async () => {
    mockedAxios.post.mockResolvedValue({ status: 204 });
    render(
      <VideoDetail
        video={{
          id: '1',
          title: 'Video 1',
          description: 'Description',
          duration: null,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
        }}
      />
    );

    const player = document.querySelector('video')!;
    fireEvent.play(player);
    fireEvent.play(player);

    await waitFor(() => expect(mockedAxios.post).toHaveBeenCalledWith(expect.stringContaining('/videos/1/views')));
    expect(mockedAxios.post).toHaveBeenCalledTimes(1);
  });

  it('allows the owner to edit and saves metadata', async () => {
    mockedAxios.put.mockResolvedValue({ status: 200 });
    const onUpdated = jest.fn().mockResolvedValue(undefined);
    render(
      <VideoDetail
        canEdit
        onUpdated={onUpdated}
        video={{
          id: 'owner-video',
          title: 'Old title',
          description: 'Old description',
          duration: null,
          thumbnailUrl: '/media/owner.webp',
          videoUrl: '/media/owner.mp4',
          tags: ['old'],
          category: 'Music',
        }}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: 'Edit metadata' }));
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'New title' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'New description' } });
    fireEvent.change(screen.getByLabelText('Tags'), { target: { value: 'music, live' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Live music' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save changes' }));

    await waitFor(() =>
      expect(mockedAxios.put).toHaveBeenCalledWith(
        expect.stringContaining('/videos/owner-video'),
        expect.objectContaining({ title: 'New title', tags: ['music', 'live'] }),
        { withCredentials: true }
      )
    );
    expect(await screen.findByRole('button', { name: 'Edit metadata' })).toBeInTheDocument();
    expect(onUpdated).toHaveBeenCalled();
  });

  it('cancels metadata editing without saving', () => {
    render(
      <VideoDetail
        canEdit
        video={{
          id: 'video',
          title: 'Title',
          description: 'Description',
          duration: null,
          thumbnailUrl: '/media/video.webp',
          videoUrl: '/media/video.mp4',
        }}
      />
    );
    fireEvent.click(screen.getByRole('button', { name: 'Edit metadata' }));
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.getByRole('button', { name: 'Edit metadata' })).toBeInTheDocument();
  });

  it('shows update errors and hides editor for non-owners', () => {
    const { rerender } = render(
      <VideoDetail
        video={{
          id: 'video',
          title: 'Title',
          description: 'Description',
          duration: null,
          thumbnailUrl: '/media/video.webp',
          videoUrl: '/media/video.mp4',
        }}
      />
    );
    expect(screen.queryByRole('button', { name: 'Edit metadata' })).not.toBeInTheDocument();

    mockedAxios.put.mockRejectedValue({
      response: { data: { message: 'You cannot edit this video.' } },
      isAxiosError: true,
    });
    mockedAxios.isAxiosError.mockReturnValue(true);
    rerender(
      <VideoDetail
        canEdit
        video={{
          id: 'video',
          title: 'Title',
          description: 'Description',
          duration: null,
          thumbnailUrl: '/media/video.webp',
          videoUrl: '/media/video.mp4',
        }}
      />
    );
    fireEvent.click(screen.getByRole('button', { name: 'Edit metadata' }));
    fireEvent.click(screen.getByRole('button', { name: 'Save changes' }));
    return waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('You cannot edit this video.'));
  });
});
