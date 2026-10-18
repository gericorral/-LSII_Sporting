import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import axios from 'axios';
import RegistrationForm from '../RegistrationForm';

jest.mock('axios');

const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('RegistrationForm', () => {
  beforeEach(() => jest.clearAllMocks());

  it('shows client validation errors without calling the API', () => {
    render(<RegistrationForm onRegistered={jest.fn()} />);
    fireEvent.click(screen.getByRole('button', { name: 'Create my account' }));

    expect(screen.getByText('Enter a valid email address.')).toBeInTheDocument();
    expect(screen.getByText('Use 3-50 letters, numbers or underscores.')).toBeInTheDocument();
    expect(screen.getByText('Password must contain at least 8 characters.')).toBeInTheDocument();
    expect(mockedAxios.post).not.toHaveBeenCalled();
  });

  it('submits valid data and reports the authenticated user', async () => {
    mockedAxios.post.mockResolvedValue({
      data: { email: 'user@example.com', username: 'player_1' },
    });
    const onRegistered = jest.fn();
    render(<RegistrationForm onRegistered={onRegistered} />);

    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'player_1' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create my account' }));

    await waitFor(() =>
      expect(onRegistered).toHaveBeenCalledWith({
        email: 'user@example.com',
        username: 'player_1',
      })
    );
    expect(mockedAxios.post).toHaveBeenCalledWith(
      expect.stringContaining('/auth/register'),
      { email: 'user@example.com', username: 'player_1', password: 'secret123' },
      { withCredentials: true }
    );
  });

  it('shows duplicate field errors returned by the API', async () => {
    mockedAxios.isAxiosError.mockReturnValue(true);
    mockedAxios.post.mockRejectedValue({
      response: { status: 409, data: { field: 'email', message: 'Email is already registered' } },
      isAxiosError: true,
    });
    render(<RegistrationForm onRegistered={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'player_1' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create my account' }));

    await waitFor(() => expect(screen.getByText('Email is already registered')).toBeInTheDocument());
  });

  it('shows a server error for an unexpected API response', async () => {
    mockedAxios.isAxiosError.mockReturnValue(true);
    mockedAxios.post.mockRejectedValue({ response: { status: 500, data: {} } });
    render(<RegistrationForm onRegistered={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'player_1' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create my account' }));

    await waitFor(() => expect(screen.getByText('Registration failed. Please try again.')).toBeInTheDocument());
  });

  it('shows a generic error for a non-Axios failure', async () => {
    mockedAxios.isAxiosError.mockReturnValue(false);
    mockedAxios.post.mockRejectedValue(new Error('offline'));
    render(<RegistrationForm onRegistered={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'player_1' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create my account' }));

    await waitFor(() => expect(screen.getByText('Registration failed. Please try again.')).toBeInTheDocument());
  });
});
