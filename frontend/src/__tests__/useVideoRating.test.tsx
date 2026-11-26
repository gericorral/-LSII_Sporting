import { act, fireEvent, render, renderHook, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';

import { useVideoRating } from '../useVideoRating';

jest.mock('axios');

const mockedAxios = jest.mocked(axios);

function Harness({ enabled = true, id = 'video' }: { enabled?: boolean; id?: string }) {
  const { rating, message, saveRating } = useVideoRating(id, enabled);
  return (
    <div>
      <span>{rating?.averageRating}</span>
      <span>{message}</span>
      <button type="button" onClick={() => saveRating(4)}>
        Save
      </button>
    </div>
  );
}

describe('useVideoRating', () => {
  afterEach(() => jest.clearAllMocks());

  it('loads and saves a rating', async () => {
    mockedAxios.get.mockResolvedValue({
      data: { videoId: 'video', averageRating: 4, ratingCount: 1, userRating: 4 },
    });
    mockedAxios.put.mockResolvedValue({
      data: { videoId: 'video', averageRating: 4.5, ratingCount: 2, userRating: 4 },
    });
    render(<Harness />);

    await waitFor(() => expect(screen.getByText('4')).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() => expect(screen.getByText('4.5')).toBeInTheDocument());
  });

  it('reports loading and saving errors', async () => {
    mockedAxios.get.mockRejectedValueOnce(new Error('load'));
    const { rerender } = render(<Harness />);
    await waitFor(() => expect(screen.getByText('The video rating could not be loaded.')).toBeInTheDocument());

    mockedAxios.get.mockResolvedValueOnce({ data: { averageRating: 0, ratingCount: 0, userRating: null } });
    mockedAxios.put.mockRejectedValueOnce({
      response: { data: { message: 'Invalid rating.' } },
    });
    mockedAxios.isAxiosError.mockReturnValue(true);
    rerender(<Harness id="video-after-load-error" />);
    await waitFor(() => expect(screen.getByText('0')).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));
    await waitFor(() => expect(screen.getByText('Invalid rating.')).toBeInTheDocument());
  });

  it('uses the fallback for an unknown save error and protects anonymous users', async () => {
    mockedAxios.get.mockResolvedValue({ data: { averageRating: 0, ratingCount: 0, userRating: null } });
    render(<Harness enabled={false} />);
    await waitFor(() => expect(screen.getByText('0')).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));
    expect(screen.getByText('Log in to rate videos.')).toBeInTheDocument();

    mockedAxios.isAxiosError.mockReturnValue(false);
    mockedAxios.put.mockRejectedValueOnce(new Error('save'));
    const { rerender } = render(<Harness />);
    await waitFor(() => expect(screen.getAllByText('0').length).toBeGreaterThan(0));
    fireEvent.click(screen.getAllByRole('button', { name: 'Save' })[1]);
    await waitFor(() => expect(screen.getAllByText('The video rating could not be saved.').length).toBeGreaterThan(0));
    rerender(<Harness />);
  });

  it('rejects unsafe video identifiers without making requests', async () => {
    render(<Harness id="a/b" />);

    expect(await screen.findByText('The video rating could not be loaded.')).toBeInTheDocument();
    expect(mockedAxios.get).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: 'Save' }));
    expect(screen.getByText('The video rating could not be saved.')).toBeInTheDocument();
    expect(mockedAxios.put).not.toHaveBeenCalled();
  });

  it('ignores stale rating responses after the video changes', async () => {
    let resolveStaleRequest: ((value: { data: { videoId: string; averageRating: number } }) => void) | undefined;
    mockedAxios.get
      .mockReturnValueOnce(
        new Promise((resolve) => {
          resolveStaleRequest = resolve;
        }) as never
      )
      .mockResolvedValueOnce({ data: { videoId: 'second', averageRating: 2, ratingCount: 1, userRating: null } });
    const { result, rerender } = renderHook(({ id }) => useVideoRating(id, true), {
      initialProps: { id: 'first' },
    });

    rerender({ id: 'second' });
    await waitFor(() => expect(result.current.rating?.averageRating).toBe(2));
    await act(async () => resolveStaleRequest?.({ data: { videoId: 'first', averageRating: 5 } }));

    expect(result.current.rating?.averageRating).toBe(2);
    expect(result.current.loading).toBe(false);
  });

  it('does not update rating state when a stale request fails', async () => {
    let rejectStaleRequest: ((error: Error) => void) | undefined;
    mockedAxios.get
      .mockReturnValueOnce(
        new Promise((_, reject) => {
          rejectStaleRequest = reject;
        }) as never
      )
      .mockResolvedValueOnce({ data: { videoId: 'second', averageRating: 2, ratingCount: 1, userRating: null } });
    const { result, rerender } = renderHook(({ id }) => useVideoRating(id, true), {
      initialProps: { id: 'first' },
    });

    rerender({ id: 'second' });
    await waitFor(() => expect(result.current.rating?.averageRating).toBe(2));
    await act(async () => rejectStaleRequest?.(new Error('stale failure')));

    expect(result.current.message).toBe('');
    expect(result.current.rating?.averageRating).toBe(2);
  });
});
