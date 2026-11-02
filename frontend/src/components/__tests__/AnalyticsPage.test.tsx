import { render, screen } from '@testing-library/react';
import axios from 'axios';
import AnalyticsPage from '../AnalyticsPage';

jest.mock('axios');

const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('AnalyticsPage', () => {
  it('shows analytics for owned videos', async () => {
    mockedAxios.get.mockResolvedValue({
      data: [{ videoId: '1', title: 'Training', views: 12, comments: 3, likes: 4 }],
    });

    render(<AnalyticsPage onBack={jest.fn()} />);

    expect(await screen.findByRole('heading', { name: 'Training' })).toBeInTheDocument();
    expect(screen.getByText('12')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
  });

  it('shows an empty state when the user has no videos', async () => {
    mockedAxios.get.mockResolvedValue({ data: [] });

    render(<AnalyticsPage onBack={jest.fn()} />);

    expect(await screen.findByText('You have not uploaded any videos yet.')).toBeInTheDocument();
  });

  it('shows a loading error', async () => {
    mockedAxios.get.mockRejectedValue(new Error('offline'));

    render(<AnalyticsPage onBack={jest.fn()} />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Unable to load your analytics.');
  });
});
