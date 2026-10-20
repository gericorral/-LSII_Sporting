import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';
import UploadForm from '../UploadForm';

jest.mock('axios');
const mockedAxios = jest.mocked(axios);

describe('UploadForm', () => {
  afterEach(() => jest.clearAllMocks());

  it('validates required fields and MP4 format', () => {
    render(<UploadForm onUploaded={jest.fn()} />);
    fireEvent.click(screen.getByRole('button', { name: 'Upload video' }));
    expect(screen.getByRole('alert')).toHaveTextContent('Title, description and an MP4 video are required.');
  });

  it('rejects unsupported formats before sending a request', () => {
    render(<UploadForm onUploaded={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'My video' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'A description' } });
    fireEvent.change(screen.getByLabelText('MP4 video'), {
      target: { files: [new File(['video'], 'video.avi', { type: 'video/x-msvideo' })] },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Upload video' }));

    expect(screen.getByRole('alert')).toHaveTextContent('Only MP4 video files are supported.');
    expect(mockedAxios.post).not.toHaveBeenCalled();
  });

  it('uploads a valid MP4 and returns to the catalog', async () => {
    const onUploaded = jest.fn();
    mockedAxios.post.mockResolvedValue({ status: 201 });
    render(<UploadForm onUploaded={onUploaded} />);
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'My video' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'A description' } });
    const file = new File(['video'], 'video.mp4', { type: 'video/mp4' });
    fireEvent.change(screen.getByLabelText('MP4 video'), { target: { files: [file] } });
    fireEvent.click(screen.getByRole('button', { name: 'Upload video' }));

    await waitFor(() => expect(onUploaded).toHaveBeenCalled());
    expect(mockedAxios.post).toHaveBeenCalledWith(expect.stringContaining('/videos'), expect.any(FormData), {
      withCredentials: true,
    });
  });

  it('shows server upload errors', async () => {
    mockedAxios.post.mockRejectedValue({
      isAxiosError: true,
      response: { data: { message: 'Storage unavailable' } },
    });
    jest.spyOn(axios, 'isAxiosError').mockReturnValue(true);
    render(<UploadForm onUploaded={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'My video' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'A description' } });
    fireEvent.change(screen.getByLabelText('MP4 video'), {
      target: { files: [new File(['video'], 'video.mp4', { type: 'video/mp4' })] },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Upload video' }));

    expect(await screen.findByText('Storage unavailable')).toBeInTheDocument();
  });
});
