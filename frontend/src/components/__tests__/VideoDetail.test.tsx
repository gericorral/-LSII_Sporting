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
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { userId: 'author-1', username: 'creator', displayName: 'Creator', avatarPath: null },
    });
    render(
      <VideoDetail
        video={{
          id: '1',
          title: 'Video 1',
          description: 'A description',
          duration: 42,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
          ownerId: 'author-1',
          ownerUsername: 'creator',
        }}
      />
    );

    expect(screen.getByRole('heading', { name: 'Video 1' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /back to videos/i })).toHaveAttribute('href', '#');
    expect(screen.getByText('A description')).toBeInTheDocument();
    expect(document.querySelector('video')).toHaveAttribute('src', '/media/1.mp4');
  });

  it('shows the video author and lets an authenticated user subscribe', async () => {
    mockedAxios.get.mockImplementation((url) => {
      if (String(url).includes('/profiles/author-1/subscription')) {
        return Promise.resolve({ data: { subscribed: false } });
      }
      if (String(url).includes('/profiles/author-1')) {
        return Promise.resolve({
          data: { userId: 'author-1', username: 'creator', displayName: 'Creator', avatarPath: 'avatar.png' },
        });
      }
      if (String(url).includes('/rating')) {
        return Promise.resolve({ data: { videoId: '1', averageRating: 0, ratingCount: 0, userRating: null } });
      }
      return Promise.resolve({ status: 200, data: { content: [], hasNext: false } });
    });
    mockedAxios.put.mockResolvedValue({ data: { subscribed: true } });
    render(
      <VideoDetail
        loggedUser={{ id: 'viewer-1', username: 'viewer' }}
        video={{
          id: '1',
          title: 'Video 1',
          description: 'A description',
          duration: 42,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
          ownerId: 'author-1',
        }}
      />
    );

    expect(await screen.findByRole('link', { name: /Creator/ })).toHaveAttribute('href', '#profile/author-1');
    fireEvent.click(screen.getByRole('button', { name: 'Subscribe' }));
    await waitFor(() =>
      expect(mockedAxios.put).toHaveBeenCalledWith(
        expect.stringContaining('/profiles/author-1/subscription'),
        {},
        { withCredentials: true }
      )
    );
    expect(await screen.findByRole('button', { name: 'Unsubscribe' })).toBeInTheDocument();
  });

  it('shows the author subscription prompt to anonymous viewers', async () => {
    mockedAxios.get.mockImplementation((url) => {
      if (String(url).includes('/profiles/author-1')) {
        return Promise.resolve({
          data: { userId: 'author-1', username: 'creator', displayName: null, avatarPath: null },
        });
      }
      return Promise.resolve({ status: 200, data: { content: [], hasNext: false } });
    });
    render(
      <VideoDetail
        video={{
          id: '1',
          title: 'Video 1',
          description: 'A description',
          duration: 42,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
          ownerId: 'author-1',
        }}
      />
    );

    expect(await screen.findByRole('button', { name: 'Subscribe' })).toBeDisabled();
    expect(screen.getByText('Log in to subscribe.')).toBeInTheDocument();
  });

  it('shows an error when subscribing to the author fails', async () => {
    mockedAxios.get.mockImplementation((url) => {
      if (String(url).includes('/profiles/author-1')) {
        return Promise.resolve({
          data: { userId: 'author-1', username: 'creator', displayName: 'Creator', avatarPath: null },
        });
      }
      return Promise.resolve({ status: 200, data: { content: [], hasNext: false } });
    });
    mockedAxios.put.mockRejectedValue({
      response: { data: { message: 'Subscription unavailable' } },
      isAxiosError: true,
    });
    mockedAxios.isAxiosError.mockReturnValue(true);
    render(
      <VideoDetail
        loggedUser={{ id: 'viewer-1', username: 'viewer' }}
        video={{
          id: '1',
          title: 'Video 1',
          description: 'A description',
          duration: 42,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
          ownerId: 'author-1',
        }}
      />
    );

    fireEvent.click(await screen.findByRole('button', { name: 'Subscribe' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Subscription unavailable');
  });

  it('handles an unavailable author profile', async () => {
    mockedAxios.get.mockRejectedValue(new Error('offline'));
    render(
      <VideoDetail
        video={{
          id: '1',
          title: 'Video 1',
          description: 'A description',
          duration: 42,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
          ownerId: 'author-1',
        }}
      />
    );

    expect(await screen.findByLabelText('Video author')).toBeInTheDocument();
    expect(screen.getByText('Unknown author')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Subscribe' })).toBeDisabled();
  });

  it('loads the rating and lets an authenticated user rate the video', async () => {
    mockedAxios.get.mockImplementation((url) => {
      if (String(url).includes('/rating')) {
        return Promise.resolve({
          status: 200,
          data: { videoId: '1', averageRating: 4, ratingCount: 2, userRating: 3 },
        });
      }
      return Promise.resolve({ status: 200, data: { content: [], hasNext: false } });
    });
    mockedAxios.put.mockResolvedValue({
      status: 200,
      data: { videoId: '1', averageRating: 4.33, ratingCount: 3, userRating: 5 },
    });
    render(
      <VideoDetail
        loggedUser={{ username: 'player' }}
        video={{
          id: '1',
          title: 'Video 1',
          description: 'A description',
          duration: null,
          thumbnailUrl: '/media/1.webp',
          videoUrl: '/media/1.mp4',
        }}
      />
    );

    expect(await screen.findByText('2 ratings')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Rate 5 stars' }));
    await waitFor(() =>
      expect(mockedAxios.put).toHaveBeenCalledWith(
        expect.stringContaining('/videos/1/rating'),
        { rating: 5 },
        { withCredentials: true }
      )
    );
    expect(await screen.findByText('3 ratings')).toBeInTheDocument();
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

    expect(document.querySelector('.video-detail > span')).not.toBeInTheDocument();
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
