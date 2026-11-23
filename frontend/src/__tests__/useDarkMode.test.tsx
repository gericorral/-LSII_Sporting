import { act, renderHook, waitFor } from '@testing-library/react';
import axios from 'axios';
import { useDarkMode } from '../useDarkMode';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('useDarkMode', () => {
  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.put.mockReset();
  });

  it('defaults to light mode when anonymous', () => {
    const { result } = renderHook(() => useDarkMode(false));
    expect(result.current.darkMode).toBe(false);
    expect(mockedAxios.get).not.toHaveBeenCalled();
  });

  it('toggles locally for anonymous users without calling the API', async () => {
    const { result } = renderHook(() => useDarkMode(false));
    await act(() => result.current.toggleDarkMode());
    expect(result.current.darkMode).toBe(true);
    expect(mockedAxios.put).not.toHaveBeenCalled();
  });

  it('handles a missing preference request in lightweight test environments', () => {
    mockedAxios.get.mockReturnValue(undefined as never);
    const { result } = renderHook(() => useDarkMode(true));
    expect(result.current.state).toBe('ready');
    expect(result.current.darkMode).toBe(false);
  });

  it('loads and persists the selected mode', async () => {
    mockedAxios.get.mockResolvedValue({ data: { darkMode: false } });
    mockedAxios.put.mockResolvedValue({ data: { darkMode: true } });
    const { result } = renderHook(() => useDarkMode(true));

    await waitFor(() => expect(result.current.state).toBe('ready'));
    await act(() => result.current.toggleDarkMode());
    expect(result.current.darkMode).toBe(true);
    expect(mockedAxios.put).toHaveBeenCalledWith(
      expect.stringContaining('/preferences/me'),
      { darkMode: true },
      { withCredentials: true }
    );
  });

  it('keeps light mode when loading fails and rolls back a failed save', async () => {
    mockedAxios.get.mockRejectedValue(new Error('network'));
    const { result } = renderHook(() => useDarkMode(true));
    await waitFor(() => expect(result.current.state).toBe('ready'));

    mockedAxios.put.mockRejectedValue(new Error('network'));
    await act(() => result.current.toggleDarkMode());
    expect(result.current.darkMode).toBe(false);
  });

  it('ignores a preference response after unmounting', async () => {
    let resolveRequest: ((value: { data: { darkMode: boolean } }) => void) | undefined;
    mockedAxios.get.mockReturnValue(
      new Promise((resolve) => {
        resolveRequest = resolve;
      }) as never
    );
    const { unmount } = renderHook(() => useDarkMode(true));
    unmount();
    resolveRequest?.({ data: { darkMode: true } });
    await Promise.resolve();
  });
});
