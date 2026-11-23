import { render, screen, waitFor } from '@testing-library/react';
import axios from 'axios';
import FavoritesPage from '../FavoritesPage';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('FavoritesPage', () => {
  it('renders favorite videos', async () => {
    mockedAxios.get.mockResolvedValue({
      data: [
        {
          id: '1',
          title: 'Saved video',
          description: '',
          duration: null,
          thumbnailUrl: '/thumb',
          videoUrl: '/video',
        },
      ],
    });
    render(<FavoritesPage onBack={jest.fn()} />);
    expect(await screen.findByRole('heading', { name: 'Saved video' })).toBeInTheDocument();
  });

  it('shows empty and error states', async () => {
    mockedAxios.get.mockResolvedValueOnce({ data: [] });
    render(<FavoritesPage onBack={jest.fn()} />);
    expect(await screen.findByRole('status')).toHaveTextContent('no favorite videos');

    mockedAxios.get.mockRejectedValueOnce(new Error('network'));
    render(<FavoritesPage onBack={jest.fn()} />);
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Unable to load favorite videos.'));
  });
});
