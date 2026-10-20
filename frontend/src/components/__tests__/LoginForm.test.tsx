import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import axios from 'axios';
import LoginForm from '../LoginForm';

jest.mock('axios');

const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('LoginForm', () => {
  beforeEach(() => jest.clearAllMocks());

  it('validates credentials before calling the API', () => {
    render(<LoginForm onLoggedIn={jest.fn()} />);
    fireEvent.click(screen.getByRole('button', { name: 'Log in' }));
    expect(screen.getByText('Enter a valid email address.')).toBeInTheDocument();
    expect(screen.getByText('Enter your password.')).toBeInTheDocument();
    expect(mockedAxios.post).not.toHaveBeenCalled();
  });

  it('reports the authenticated user after a successful login', async () => {
    mockedAxios.post.mockResolvedValue({
      data: { email: 'user@example.com', username: 'player_1' },
    });
    const onLoggedIn = jest.fn();
    render(<LoginForm onLoggedIn={onLoggedIn} />);
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Log in' }));

    await waitFor(() =>
      expect(onLoggedIn).toHaveBeenCalledWith({
        email: 'user@example.com',
        username: 'player_1',
      })
    );
    expect(mockedAxios.post).toHaveBeenCalledWith(
      expect.stringContaining('/auth/login'),
      { email: 'user@example.com', password: 'secret123' },
      { withCredentials: true }
    );
  });

  it('shows the same error for invalid credentials', async () => {
    mockedAxios.isAxiosError.mockReturnValue(true);
    mockedAxios.post.mockRejectedValue({
      response: { status: 401, data: { message: 'Invalid email or password' } },
      isAxiosError: true,
    });
    render(<LoginForm onLoggedIn={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: 'Log in' }));

    await waitFor(() => expect(screen.getByText('Invalid email or password')).toBeInTheDocument());
  });

  it('shows a fallback for an Axios error without a server message', async () => {
    mockedAxios.isAxiosError.mockReturnValue(true);
    mockedAxios.post.mockRejectedValue({ response: { status: 500, data: {} } });
    render(<LoginForm onLoggedIn={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: 'Log in' }));

    await waitFor(() => expect(screen.getByText('Unable to log in.')).toBeInTheDocument());
  });
});
