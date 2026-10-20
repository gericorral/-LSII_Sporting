import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';

import { useVideo } from '../useVideo';

jest.mock('axios');

const mockedAxios = jest.mocked(axios);

function Harness({ id }: { id: string }) {
  const { loading, message, value } = useVideo(id);
  return (
    <div>
      <span>{loading}</span>
      <span>{message}</span>
      <span>{value?.title}</span>
    </div>
  );
}

describe('useVideo', () => {
  afterEach(() => jest.clearAllMocks());

  it('loads a video by id', async () => {
    mockedAxios.get.mockResolvedValue({
      status: 200,
      data: { id: '1', title: 'Video 1' },
    });

    render(<Harness id="1" />);

    await waitFor(() => expect(screen.getByText('Video 1')).toBeInTheDocument());
    expect(mockedAxios.get).toHaveBeenCalledWith('other/videos/1');
  });

  it('reports not found and network errors', async () => {
    mockedAxios.get.mockRejectedValueOnce({ response: { status: 404 }, message: 'Not found' });
    const { rerender } = render(<Harness id="missing" />);
    await waitFor(() => expect(screen.getByText('Video not found.')).toBeInTheDocument());

    mockedAxios.get.mockRejectedValueOnce({ message: 'Network error' });
    rerender(<Harness id="broken" />);
    await waitFor(() => expect(screen.getByText('Error fetching video: Network error')).toBeInTheDocument());
  });
});
