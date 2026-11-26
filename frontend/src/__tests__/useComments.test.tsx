import { act, renderHook, waitFor } from '@testing-library/react';
import axios from 'axios';
import { useComments } from '../useComments';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

const comment = {
  id: 'comment-1',
  videoId: 'video-1',
  authorId: 'author-1',
  authorName: 'Player',
  content: 'Nice video',
  createdAt: null,
};

describe('useComments', () => {
  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.post.mockReset();
  });

  it('loads comments, appends the next page, and updates likes', async () => {
    mockedAxios.get
      .mockResolvedValueOnce({ status: 200, data: { content: [comment], hasNext: true } })
      .mockResolvedValueOnce({ status: 200, data: { content: [], hasNext: false } });
    mockedAxios.post.mockResolvedValue({
      data: { commentId: 'comment-1', likeCount: 1, likedByCurrentUser: true },
    });

    const { result } = renderHook(() => useComments('video-1'));
    await waitFor(() => expect(result.current.loading).toBe('success'));
    expect(result.current.comments).toEqual([comment]);

    await act(async () => result.current.loadMore());
    expect(result.current.hasNext).toBe(false);
    expect(mockedAxios.get).toHaveBeenLastCalledWith('other/videos/video-1/comments', {
      params: { page: 1, size: 20 },
    });

    await act(async () => result.current.updateLike('comment-1'));
    expect(result.current.comments[0]).toMatchObject({ likeCount: 1, likedByCurrentUser: true });
  });

  it('does not make requests for an unsafe video identifier', async () => {
    const { result } = renderHook(() => useComments('../private'));

    await waitFor(() => expect(result.current.loading).toBe('error'));
    expect(result.current.message).toBe('Invalid video ID.');
    expect(result.current.comments).toEqual([]);
    expect(mockedAxios.get).not.toHaveBeenCalled();
  });

  it('reports a failed comment request', async () => {
    mockedAxios.get.mockRejectedValue(new Error('network'));
    const { result } = renderHook(() => useComments('video-1'));

    await waitFor(() => expect(result.current.loading).toBe('error'));
    expect(result.current.message).toContain('network');
  });
});
