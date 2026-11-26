import { act, renderHook, waitFor } from '@testing-library/react';
import axios from 'axios';
import { useVideoFavorite } from '../useVideoFavorite';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('useVideoFavorite', () => {
  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.put.mockReset();
    mockedAxios.delete.mockReset();
  });

  it('does not request status for anonymous users', () => {
    const { result } = renderHook(() => useVideoFavorite('1', false));
    expect(result.current.favorited).toBe(false);
    expect(mockedAxios.get).not.toHaveBeenCalled();
  });

  it('rejects unsafe video identifiers without making requests', async () => {
    const { result } = renderHook(() => useVideoFavorite('../private', true));

    await waitFor(() => expect(result.current.state).toBe('error'));
    expect(result.current.message).toBe('Unable to load favorite status.');
    expect(mockedAxios.get).not.toHaveBeenCalled();

    await act(() => result.current.toggleFavorite());
    expect(result.current.message).toBe('The favorite could not be updated.');
    expect(mockedAxios.put).not.toHaveBeenCalled();
    expect(mockedAxios.delete).not.toHaveBeenCalled();
  });

  it('loads and toggles a favorite', async () => {
    mockedAxios.get.mockResolvedValue({ data: { favorited: false } });
    mockedAxios.put.mockResolvedValue({ data: { favorited: true } });
    mockedAxios.delete.mockResolvedValue({ data: { favorited: false } });
    const { result } = renderHook(() => useVideoFavorite('1', true));

    await waitFor(() => expect(result.current.state).toBe('ready'));
    await act(() => result.current.toggleFavorite());
    expect(mockedAxios.put).toHaveBeenCalled();
    expect(result.current.favorited).toBe(true);
    await act(() => result.current.toggleFavorite());
    expect(mockedAxios.delete).toHaveBeenCalled();
    expect(result.current.favorited).toBe(false);
  });

  it('exposes loading and saving errors', async () => {
    mockedAxios.get.mockRejectedValue(new Error('network'));
    const { result } = renderHook(() => useVideoFavorite('1', true));
    await waitFor(() => expect(result.current.message).toBe('Unable to load favorite status.'));

    mockedAxios.get.mockResolvedValue({ data: { favorited: false } });
    mockedAxios.put.mockRejectedValue(new Error('network'));
    const second = renderHook(() => useVideoFavorite('1', true));
    await waitFor(() => expect(second.result.current.state).toBe('ready'));
    await act(() => second.result.current.toggleFavorite());
    expect(second.result.current.message).toBe('The favorite could not be updated.');
  });

  it('ignores a stale favorite response after the video changes', async () => {
    let rejectStaleRequest: ((error: Error) => void) | undefined;
    mockedAxios.get
      .mockReturnValueOnce(
        new Promise((_, reject) => {
          rejectStaleRequest = reject;
        }) as never
      )
      .mockResolvedValueOnce({ data: { favorited: false } });
    const { result, rerender } = renderHook(({ id }) => useVideoFavorite(id, true), {
      initialProps: { id: 'first-video' },
    });

    rerender({ id: 'second-video' });
    await waitFor(() => expect(result.current.state).toBe('ready'));
    await act(async () => rejectStaleRequest?.(new Error('stale failure')));

    expect(result.current.favorited).toBe(false);
    expect(result.current.message).toBe('');
  });

  it('does not apply an older successful favorite response', async () => {
    let resolveStaleRequest: ((value: { data: { favorited: boolean } }) => void) | undefined;
    mockedAxios.get
      .mockReturnValueOnce(
        new Promise((resolve) => {
          resolveStaleRequest = resolve;
        }) as never
      )
      .mockResolvedValueOnce({ data: { favorited: false } });
    const { result, rerender } = renderHook(({ id }) => useVideoFavorite(id, true), {
      initialProps: { id: 'first-video' },
    });

    rerender({ id: 'second-video' });
    await waitFor(() => expect(result.current.state).toBe('ready'));
    await act(async () => resolveStaleRequest?.({ data: { favorited: true } }));

    expect(result.current.favorited).toBe(false);
  });
});
