import { act, renderHook, waitFor } from '@testing-library/react';
import axios from 'axios';
import { useAnalytics } from '../useAnalytics';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('useAnalytics', () => {
  beforeEach(() => {
    mockedAxios.get.mockReset();
  });

  it('does not request analytics while disabled', async () => {
    const { result } = renderHook(() => useAnalytics(false));

    await act(async () => result.current.refresh());

    expect(result.current.value).toEqual([]);
    expect(result.current.loading).toBe('loading');
    expect(mockedAxios.get).not.toHaveBeenCalled();
  });

  it('loads analytics and exposes request failures', async () => {
    mockedAxios.get.mockResolvedValue({ data: [] });
    const { result } = renderHook(() => useAnalytics(true));
    await waitFor(() => expect(result.current.loading).toBe('success'));

    mockedAxios.get.mockResolvedValueOnce({
      data: [{ videoId: '1', title: 'Video', views: 2, comments: 1, likes: 0 }],
    });
    await act(async () => result.current.refresh());
    expect(result.current.value).toHaveLength(1);
    expect(result.current.loading).toBe('success');

    mockedAxios.get.mockRejectedValueOnce(new Error('offline'));
    await act(async () => result.current.refresh());
    expect(result.current.message).toBe('Unable to load your analytics.');
  });
});
