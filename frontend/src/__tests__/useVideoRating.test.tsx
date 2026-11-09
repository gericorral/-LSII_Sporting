import { fireEvent, render, screen, waitFor } from '@testing-library/react';
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
});
