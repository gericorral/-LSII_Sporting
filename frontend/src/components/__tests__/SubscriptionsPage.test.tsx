import { render, screen } from '@testing-library/react';
import axios from 'axios';
import SubscriptionsPage from '../SubscriptionsPage';

jest.mock('axios');

const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('SubscriptionsPage', () => {
  beforeEach(() => jest.clearAllMocks());

  it('renders subscribed profiles', async () => {
    mockedAxios.get.mockResolvedValue({
      data: [
        { userId: '2', username: 'creator', displayName: 'Creator', avatarPath: 'avatar.png' },
        { userId: '3', username: 'viewer', displayName: null, avatarPath: null },
      ],
    });

    render(<SubscriptionsPage onBack={jest.fn()} />);

    expect(await screen.findByRole('link', { name: /Creator/ })).toHaveAttribute('href', '#profile/2');
    expect(screen.getByRole('img', { name: "creator's avatar" })).toHaveAttribute('src', 'avatar.png');
    expect(screen.getByText('V')).toBeInTheDocument();
  });

  it('renders an empty state and load errors', async () => {
    mockedAxios.get.mockResolvedValue({ data: [] });
    const view = render(<SubscriptionsPage onBack={jest.fn()} />);
    expect(await screen.findByRole('status')).toHaveTextContent('not subscribed');

    mockedAxios.get.mockRejectedValue(new Error('offline'));
    view.unmount();
    render(<SubscriptionsPage onBack={jest.fn()} />);
    expect(await screen.findByRole('alert')).toHaveTextContent('Unable to load');
  });
});
