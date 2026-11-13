import { FormEvent, useState } from 'react';
import axios from 'axios';
import { getEnv } from '../utils/Env';
import { useTranslation } from 'react-i18next';

type UploadFormProps = {
  onUploaded: () => void;
};

export default function UploadForm({ onUploaded }: UploadFormProps) {
  const { t } = useTranslation();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [video, setVideo] = useState<File | null>(null);
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    if (!title.trim() || !description.trim() || !video || !thumbnail) {
      setError(t('upload.required'));
      return;
    }
    if (video.type !== 'video/mp4') {
      setError(t('upload.mp4Only'));
      return;
    }
    if (video.size > 100 * 1024 * 1024) {
      setError(t('upload.maxSize'));
      return;
    }
    if (!['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(thumbnail.type)) {
      setError(t('upload.imageOnly'));
      return;
    }

    const data = new FormData();
    data.append('title', title.trim());
    data.append('description', description.trim());
    data.append('video', video);
    data.append('thumbnail', thumbnail);
    try {
      setSubmitting(true);
      await axios.post(`${getEnv().API_BASE_URL}/videos`, data, { withCredentials: true });
      onUploaded();
    } catch (uploadError: unknown) {
      if (axios.isAxiosError<{ message?: string }>(uploadError)) {
        setError(uploadError.response?.data?.message || t('upload.failed'));
      } else {
        setError(t('upload.failed'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="registration-panel" aria-labelledby="upload-title">
      <div className="registration-heading">
        <p className="registration-eyebrow">{t('upload.eyebrow')}</p>
        <h2 id="upload-title">{t('upload.title')}</h2>
        <p>{t('upload.description')}</p>
      </div>
      <form onSubmit={submit} noValidate>
        <div className="registration-field">
          <label htmlFor="upload-title-field">{t('upload.titleLabel')}</label>
          <input
            id="upload-title-field"
            value={title}
            maxLength={255}
            onChange={(event) => setTitle(event.target.value)}
            required
          />
        </div>
        <div className="registration-field">
          <label htmlFor="upload-thumbnail">{t('upload.thumbnail')}</label>
          <input
            id="upload-thumbnail"
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif,.jpg,.jpeg,.png,.webp,.gif"
            onChange={(event) => setThumbnail(event.target.files?.[0] || null)}
            required
          />
        </div>
        <div className="registration-field">
          <label htmlFor="upload-description">{t('video.description')}</label>
          <textarea
            id="upload-description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            required
          />
        </div>
        <div className="registration-field">
          <label htmlFor="upload-video">{t('upload.video')}</label>
          <input
            id="upload-video"
            type="file"
            accept="video/mp4,.mp4"
            onChange={(event) => setVideo(event.target.files?.[0] || null)}
            required
          />
        </div>
        {error && <p role="alert">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? t('upload.uploading') : t('uploadVideo')}
        </button>
      </form>
      <p className="registration-footer">
        <a href="#">{t('backToVideos')}</a>
      </p>
    </section>
  );
}
