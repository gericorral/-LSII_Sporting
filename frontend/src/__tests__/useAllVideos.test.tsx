import { act, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';

import { useAllVideos } from '../useAllVideos';

jest.mock('axios');

const mockedAxios = jest.mocked(axios);

function Harness() {
  const { loading, message, value, hasNext, loadMore, search } = useAllVideos();
  return (
    <div>
      <span>{loading}</span>
      <span>{message}</span>
      <span>{value.length}</span>
      <span>{hasNext ? 'more' : 'done'}</span>
      <button onClick={loadMore}>load more</button>
      <button onClick={() => search('travel')}>search</button>
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

  it('searches with a trimmed query and resets pagination', async () => {
    mockedAxios.get
      .mockResolvedValueOnce({
        status: 200,
        data: { content: [{ id: '1' }], hasNext: false },
      })
      .mockResolvedValueOnce({
        status: 200,
        data: { content: [{ id: '2' }], hasNext: false },
      });

    render(<Harness />);
    await waitFor(() => expect(screen.getByText('success')).toBeInTheDocument());
    await act(async () => screen.getByRole('button', { name: 'search' }).click());

    await waitFor(() =>
      expect(mockedAxios.get).toHaveBeenLastCalledWith('other/videos', {
        params: { page: 0, size: 12, query: 'travel' },
      })
    );
  });

  it('reports errors while loading more search results', async () => {
    mockedAxios.get
      .mockResolvedValueOnce({
        status: 200,
        data: { content: [{ id: '1' }], hasNext: true },
      })
      .mockRejectedValueOnce({ message: 'Search failed' });

    render(<Harness />);
    await waitFor(() => expect(screen.getByText('more')).toBeInTheDocument());
    await act(async () => screen.getByRole('button', { name: 'load more' }).click());

    await waitFor(() => expect(screen.getByText('Error fetching more videos: Search failed')).toBeInTheDocument());
  });

  it('does not request another page when pagination is unavailable', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { content: [], hasNext: false },
    });

    render(<Harness />);
    await waitFor(() => expect(screen.getByText('done')).toBeInTheDocument());
    await act(async () => screen.getByRole('button', { name: 'load more' }).click());

    expect(mockedAxios.get).toHaveBeenCalledTimes(1);
  });

  it('keeps the existing results when loading more returns a non-success response', async () => {
    mockedAxios.get
      .mockResolvedValueOnce({
        status: 200,
        data: { content: [{ id: '1' }], hasNext: true },
      })
      .mockResolvedValueOnce({
        status: 204,
        data: { content: [{ id: 'ignored' }], hasNext: false },
      });

    render(<Harness />);
    await waitFor(() => expect(screen.getByText('more')).toBeInTheDocument());
    await act(async () => screen.getByRole('button', { name: 'load more' }).click());

    await waitFor(() => expect(screen.getByText('success')).toBeInTheDocument());
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('more')).toBeInTheDocument();
  });

  it('ignores a stale catalog response after a newer search completes', async () => {
    let resolveInitial: ((value: unknown) => void) | undefined;
    const initialRequest = new Promise((resolve) => {
      resolveInitial = resolve;
    });
    mockedAxios.get.mockReturnValueOnce(initialRequest as never).mockResolvedValueOnce({
      status: 200,
      data: { content: [{ id: 'search-result' }], hasNext: false },
    });

    render(<Harness />);
    await act(async () => screen.getByRole('button', { name: 'search' }).click());
    await waitFor(() => expect(screen.getByText('1')).toBeInTheDocument());

    resolveInitial?.({
      status: 200,
      data: { content: [{ id: 'stale-result' }], hasNext: false },
    });
    await act(async () => await initialRequest);

    expect(screen.getByText('1')).toBeInTheDocument();
  });
});
