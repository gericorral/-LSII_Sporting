import { FormEvent, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';

type UploadFormProps = {
  onUploaded: () => void;
};

export default function UploadForm({ onUploaded }: UploadFormProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [video, setVideo] = useState<File | null>(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    if (!title.trim() || !description.trim() || !video) {
      setError('Title, description and an MP4 video are required.');
      return;
    }
    if (video.type !== 'video/mp4') {
      setError('Only MP4 video files are supported.');
      return;
    }
    if (video.size > 100 * 1024 * 1024) {
      setError('Video files must not exceed 100 MB.');
      return;
    }

    const data = new FormData();
    data.append('title', title.trim());
    data.append('description', description.trim());
    data.append('video', video);
    try {
      setSubmitting(true);
      await axios.post(`${getEnv().API_BASE_URL}/videos`, data, { withCredentials: true });
      onUploaded();
    } catch (uploadError: unknown) {
      if (axios.isAxiosError<{ message?: string }>(uploadError)) {
        setError(uploadError.response?.data?.message || 'The video could not be uploaded.');
      } else {
        setError('The video could not be uploaded.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="registration-panel" aria-labelledby="upload-title">
      <div className="registration-heading">
        <p className="registration-eyebrow">SHARE YOUR CONTENT</p>
        <h2 id="upload-title">Upload a video</h2>
        <p>Publish an MP4 video for the ProTube community.</p>
      </div>
      <form onSubmit={submit} noValidate>
        <div className="registration-field">
          <label htmlFor="upload-title-field">Title</label>
          <input
            id="upload-title-field"
            value={title}
            maxLength={255}
            onChange={(event) => setTitle(event.target.value)}
          />
        </div>
        <div className="registration-field">
          <label htmlFor="upload-description">Description</label>
          <textarea
            id="upload-description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
        </div>
        <div className="registration-field">
          <label htmlFor="upload-video">MP4 video</label>
          <input
            id="upload-video"
            type="file"
            accept="video/mp4,.mp4"
            onChange={(event) => setVideo(event.target.files?.[0] || null)}
          />
        </div>
        {error && <p role="alert">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Uploading...' : 'Upload video'}
        </button>
      </form>
      <p className="registration-footer">
        <a href="#">Back to videos</a>
      </p>
    </section>
  );
}
