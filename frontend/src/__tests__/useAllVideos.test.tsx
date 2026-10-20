import { act, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';

import { useAllVideos } from '../useAllVideos';

jest.mock('axios');

const mockedAxios = jest.mocked(axios);

function Harness() {
  const { loading, message, value, hasNext, loadMore } = useAllVideos();
  return (
    <div>
      <span>{loading}</span>
      <span>{message}</span>
      <span>{value.length}</span>
      <span>{hasNext ? 'more' : 'done'}</span>
      <button onClick={loadMore}>load more</button>
    </div>
  );
}

describe('useAllVideos', () => {
  afterEach(() => jest.clearAllMocks());

  it('loads the catalog', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { content: [{ id: '1' }], hasNext: false },
    });

    render(<Harness />);

    await waitFor(() => expect(screen.getByText('success')).toBeInTheDocument());
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(mockedAxios.get).toHaveBeenCalledWith('other/videos', { params: { page: 0, size: 12 } });
  });

  it('reports request errors', async () => {
    mockedAxios.get.mockRejectedValue({ message: 'Network error' });

    render(<Harness />);

    await waitFor(() => expect(screen.getByText('Error fetching videos: Network error')).toBeInTheDocument());
  });

  it('keeps an empty catalog for a non-success response', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 204,
      data: { content: [{ id: 'ignored' }], hasNext: false },
    });

    render(<Harness />);

    await waitFor(() => expect(screen.getByText('success')).toBeInTheDocument());
    expect(screen.getByText('0')).toBeInTheDocument();
  });

  it('loads and appends the next page', async () => {
    mockedAxios.get
      .mockResolvedValueOnce({
        status: 200,
        data: { content: [{ id: '1' }], hasNext: true },
      })
      .mockResolvedValueOnce({
        status: 200,
        data: { content: [{ id: '2' }], hasNext: false },
      });

    render(<Harness />);
    await waitFor(() => expect(screen.getByText('more')).toBeInTheDocument());
    await act(async () => {
      screen.getByRole('button', { name: 'load more' }).click();
    });

    await waitFor(() => expect(screen.getByText('2')).toBeInTheDocument());
    expect(mockedAxios.get).toHaveBeenLastCalledWith('other/videos', { params: { page: 1, size: 12 } });
  });
});
