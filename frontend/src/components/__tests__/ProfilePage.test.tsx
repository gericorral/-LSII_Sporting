import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import axios from 'axios';
import ProfilePage from '../ProfilePage';

jest.mock('axios');

const mockedAxios = axios as jest.Mocked<typeof axios>;
const profile = {
  userId: '1',
  username: 'player_1',
  displayName: 'Player One',
  description: 'A video fan',
  avatarPath: null,
  website: 'https://example.com',
  location: 'Barcelona',
  publicProfile: true,
};

describe('ProfilePage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(axios, 'isAxiosError').mockReturnValue(false);
  });

  it('loads and edits the authenticated profile', async () => {
    mockedAxios.get.mockResolvedValue({ data: profile });
    mockedAxios.put.mockResolvedValue({ data: { ...profile, displayName: 'Updated' } });
    render(<ProfilePage editable onBack={jest.fn()} />);

    expect(await screen.findByRole('heading', { name: 'Player One' })).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Display name'), { target: { value: 'Updated' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Updated description' } });
    fireEvent.change(screen.getByLabelText('Avatar URL'), { target: { value: 'https://example.com/avatar.png' } });
    fireEvent.change(screen.getByLabelText('Website'), { target: { value: 'https://example.com' } });
    fireEvent.change(screen.getByLabelText('Location'), { target: { value: 'Madrid' } });
    fireEvent.click(screen.getByLabelText('Public profile'));
    fireEvent.click(screen.getByRole('button', { name: 'Save profile' }));

    await waitFor(() => expect(screen.getByText('Profile saved.')).toBeInTheDocument());
    expect(mockedAxios.put).toHaveBeenCalledWith(
      expect.stringContaining('/profiles/me'),
      expect.objectContaining({ displayName: 'Updated' }),
      { withCredentials: true }
    );
  });

  it('shows public profiles without edit controls', async () => {
    mockedAxios.get.mockResolvedValue({ data: profile });
    render(<ProfilePage userId="1" editable={false} onBack={jest.fn()} />);

    expect(await screen.findByText('A video fan')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Save profile' })).not.toBeInTheDocument();
  });

  it('renders an avatar and optional public details', async () => {
    mockedAxios.get.mockResolvedValue({
      data: {
        ...profile,
        displayName: null,
        description: null,
        avatarPath: 'https://example.com/avatar.png',
        website: null,
        location: null,
      },
    });
    render(<ProfilePage userId="1" editable={false} onBack={jest.fn()} />);

    expect(await screen.findByRole('img', { name: "player_1's avatar" })).toHaveAttribute(
      'src',
      'https://example.com/avatar.png'
    );
    expect(screen.getByRole('heading', { name: 'player_1' })).toBeInTheDocument();
    expect(screen.getByText('This user has not added a description yet.')).toBeInTheDocument();
  });

  it('renders empty editable fields when profile values are null', async () => {
    mockedAxios.get.mockResolvedValue({
      data: {
        userId: '1',
        username: 'player_1',
        displayName: null,
        description: null,
        avatarPath: null,
        website: null,
        location: null,
        publicProfile: false,
      },
    });
    render(<ProfilePage editable onBack={jest.fn()} />);

    await screen.findByRole('heading', { name: 'player_1' });
    expect(screen.getByLabelText('Display name')).toHaveValue('');
    expect(screen.getByLabelText('Website')).toHaveValue('');
    expect(screen.getByLabelText('Location')).toHaveValue('');
  });

  it('shows an error when the profile cannot be loaded', async () => {
    mockedAxios.get.mockRejectedValue(new Error('offline'));
    render(<ProfilePage editable onBack={jest.fn()} />);
    expect(await screen.findByText('Unable to load this profile.')).toBeInTheDocument();
  });

  it('shows the server error when saving fails', async () => {
    mockedAxios.get.mockResolvedValue({ data: profile });
    mockedAxios.put.mockRejectedValue({
      response: { status: 500, data: { message: 'Profile storage unavailable' } },
      isAxiosError: true,
    });
    jest.spyOn(axios, 'isAxiosError').mockReturnValue(true);
    render(<ProfilePage editable onBack={jest.fn()} />);

    await screen.findByRole('heading', { name: 'Player One' });
    fireEvent.click(screen.getByRole('button', { name: 'Save profile' }));

    expect(await screen.findByText('Profile storage unavailable')).toBeInTheDocument();
  });

  it('shows a status error when a non-Axios save failure occurs', async () => {
    mockedAxios.get.mockResolvedValue({ data: profile });
    mockedAxios.put.mockRejectedValue(new Error('offline'));
    render(<ProfilePage editable onBack={jest.fn()} />);

    await screen.findByRole('heading', { name: 'Player One' });
    fireEvent.click(screen.getByRole('button', { name: 'Save profile' }));

    expect(await screen.findByText('Unable to save your profile.')).toBeInTheDocument();
  });

  it('shows the HTTP status when the Axios error has no message', async () => {
    mockedAxios.get.mockResolvedValue({ data: profile });
    mockedAxios.isAxiosError.mockReturnValue(true);
    mockedAxios.put.mockRejectedValue({ response: { status: 500, data: {} } });
    render(<ProfilePage editable onBack={jest.fn()} />);

    await screen.findByRole('heading', { name: 'Player One' });
    fireEvent.click(screen.getByRole('button', { name: 'Save profile' }));

    expect(await screen.findByText('Unable to save your profile (500).')).toBeInTheDocument();
  });
});
