import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';

import CommentsSection from '../CommentsSection';

jest.mock('axios');
const mockedAxios = jest.mocked(axios);

const existingComment = {
  id: 'comment-1',
  videoId: 'video-1',
  authorId: 'user-1',
  authorName: 'Player',
  content: 'Useful video',
  createdAt: '2026-10-29T17:00:00',
};

describe('CommentsSection', () => {
  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.post.mockReset();
  });

  it('renders comments and a login prompt for visitors', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { content: [existingComment], hasNext: false },
    });

    render(<CommentsSection videoId="video-1" loggedUser={null} />);

    expect(screen.getByRole('link', { name: 'Log in' })).toHaveAttribute('href', '#login');
    expect(await screen.findByText('Useful video')).toBeInTheDocument();
    expect(screen.getByText('Player')).toBeInTheDocument();
    expect(screen.getByText(/2026/)).toBeInTheDocument();
  });

  it('formats the LocalDateTime array returned by the backend', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { content: [{ ...existingComment, createdAt: [2026, 10, 29, 17, 0, 0, 0] }], hasNext: false },
    });

    render(<CommentsSection videoId="video-1" loggedUser={null} />);

    const time = await screen.findByText(/2026/);
    expect(time).not.toHaveTextContent('Invalid Date');
    expect(time).toHaveAttribute('dateTime', '2026-10-29T17:00:00.000Z');
  });

  it.each([null, 'not-a-date', [2026, 13, 40]])(
    'uses a safe fallback for invalid timestamps: %p',
    async (createdAt) => {
      mockedAxios.get.mockResolvedValue({
        status: 200,
        data: { content: [{ ...existingComment, createdAt }], hasNext: false },
      });

      render(<CommentsSection videoId="video-1" loggedUser={null} />);

      expect(await screen.findByText('Date unavailable')).toBeInTheDocument();
      expect(screen.queryByText('Invalid Date')).not.toBeInTheDocument();
    }
  );

  it('validates empty and excessively long comments', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { content: [], hasNext: false },
    });
    render(<CommentsSection videoId="video-1" loggedUser={{ username: 'Player' }} />);

    const input = await screen.findByLabelText('Add a comment');
    fireEvent.click(screen.getByRole('button', { name: 'Post comment' }));
    expect(screen.getByRole('alert')).toHaveTextContent('Comment cannot be empty.');

    fireEvent.change(input, { target: { value: 'x'.repeat(2001) } });
    fireEvent.click(screen.getByRole('button', { name: 'Post comment' }));
    expect(screen.getByRole('alert')).toHaveTextContent('Comment must be at most 2000 characters.');
    expect(mockedAxios.post).not.toHaveBeenCalled();
  });

  it('posts a comment and inserts the server response immediately', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { content: [], hasNext: false },
    });
    mockedAxios.post.mockResolvedValue({
      status: 201,
      data: { ...existingComment, content: 'New comment', createdAt: [2026, 10, 29, 17, 1, 0, 0] },
    });
    render(<CommentsSection videoId="video-1" loggedUser={{ username: 'Player' }} />);

    const input = await screen.findByLabelText('Add a comment');
    fireEvent.change(input, { target: { value: '  New comment  ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Post comment' }));

    await waitFor(() => expect(screen.getByText('New comment')).toBeInTheDocument());
    expect(mockedAxios.post).toHaveBeenCalledWith(
      'other/videos/video-1/comments',
      { content: 'New comment' },
      { withCredentials: true }
    );
    expect(input).toHaveValue('');
  });

  it('loads the next comment page and displays API errors', async () => {
    mockedAxios.get
      .mockResolvedValueOnce({
        status: 200,
        data: { content: [existingComment], hasNext: true },
      })
      .mockResolvedValueOnce({
        status: 200,
        data: {
          content: [{ ...existingComment, id: 'comment-2', content: 'Second comment' }],
          hasNext: false,
        },
      });
    render(<CommentsSection videoId="video-1" loggedUser={null} />);

    await screen.findByText('Useful video');
    fireEvent.click(screen.getByRole('button', { name: 'Load more comments' }));
    expect(await screen.findByText('Second comment')).toBeInTheDocument();
    expect(mockedAxios.get).toHaveBeenLastCalledWith('other/videos/video-1/comments', {
      params: { page: 1, size: 20 },
    });
  });
});
