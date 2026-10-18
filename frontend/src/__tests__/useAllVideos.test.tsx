import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';

import { useAllVideos } from '../useAllVideos';

jest.mock('axios');

const mockedAxios = jest.mocked(axios);

function Harness() {
  const { loading, message, value } = useAllVideos();
  return (
    <div>
      <span>{loading}</span>
      <span>{message}</span>
      <span>{value.length}</span>
    </div>
  );
}

describe('useAllVideos', () => {
  afterEach(() => jest.clearAllMocks());

  it('loads the catalog', async () => {
    mockedAxios.get.mockResolvedValue({ status: 200, data: [{ id: '1' }] });

    render(<Harness />);

    await waitFor(() => expect(screen.getByText('success')).toBeInTheDocument());
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(mockedAxios.get).toHaveBeenCalledWith('other/videos');
  });

  it('reports request errors', async () => {
    mockedAxios.get.mockRejectedValue({ message: 'Network error' });

    render(<Harness />);

    await waitFor(() =>
      expect(screen.getByText('Error fetching videos: Network error')).toBeInTheDocument(),
    );
  });

  it('keeps an empty catalog for a non-success response', async () => {
    mockedAxios.get.mockResolvedValue({ status: 204, data: [{ id: 'ignored' }] });

    render(<Harness />);

    await waitFor(() => expect(screen.getByText('success')).toBeInTheDocument());
    expect(screen.getByText('0')).toBeInTheDocument();
  });
});
