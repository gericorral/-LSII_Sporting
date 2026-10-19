import type { VideoSummary } from '../useAllVideos';
import { getEnv, resolveMediaUrl } from '../utils/Env';
import { useState } from 'react';
import axios from 'axios';

type VideoDetailProps = {
  video: VideoSummary;
  canEdit?: boolean;
  onUpdated?: () => Promise<void>;
};

const VideoDetail = ({ video, canEdit = false, onUpdated }: VideoDetailProps) => {
  const [editing, setEditing] = useState(false);
  const [title, setTitle] = useState(video.title);
  const [description, setDescription] = useState(video.description);
  const [tags, setTags] = useState((video.tags || []).join(', '));
  const [category, setCategory] = useState(video.category || '');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const save = async () => {
    setError('');
    try {
      setSaving(true);
      await axios.put(`${getEnv().API_BASE_URL}/videos/${encodeURIComponent(video.id)}`, {
        title,
        description,
        tags: tags.split(',').map((tag) => tag.trim()).filter(Boolean),
        category,
      }, { withCredentials: true });
      await onUpdated?.();
      setEditing(false);
    } catch (saveError: unknown) {
      if (axios.isAxiosError<{ message?: string }>(saveError)) {
        setError(saveError.response?.data?.message || 'The video could not be updated.');
      } else {
        setError('The video could not be updated.');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <article className="video-detail">
      <a href="#">&larr; Back to videos</a>
      <h2>{video.title}</h2>
      <video controls poster={resolveMediaUrl(video.thumbnailUrl)} src={resolveMediaUrl(video.videoUrl)}>
        Your browser does not support video playback.
      </video>
      {canEdit && !editing && <button className="edit-video-button" type="button" onClick={() => setEditing(true)}>Edit metadata</button>}
      {editing ? (
        <div className="video-edit-form">
          <label htmlFor="edit-video-title">Title</label>
          <input id="edit-video-title" value={title} onChange={(event) => setTitle(event.target.value)} />
          <label htmlFor="edit-video-description">Description</label>
          <textarea id="edit-video-description" value={description} onChange={(event) => setDescription(event.target.value)} />
          <label htmlFor="edit-video-tags">Tags</label>
          <input id="edit-video-tags" value={tags} onChange={(event) => setTags(event.target.value)} placeholder="music, live, tutorial" />
          <label htmlFor="edit-video-category">Category</label>
          <input id="edit-video-category" value={category} onChange={(event) => setCategory(event.target.value)} />
          {error && <p role="alert">{error}</p>}
          <div className="video-edit-actions">
            <button type="button" onClick={save} disabled={saving}>{saving ? 'Saving...' : 'Save changes'}</button>
            <button type="button" onClick={() => setEditing(false)}>Cancel</button>
          </div>
        </div>
      ) : (
        <>
          <p>{video.description || 'No description available.'}</p>
          {video.category && <span className="video-category">{video.category}</span>}
          {video.tags?.length ? <p className="video-tags">{video.tags.map((tag) => `#${tag}`).join(' ')}</p> : null}
        </>
      )}
      {video.duration !== null && <span>{video.duration}s</span>}
    </article>
  );
};

export default VideoDetail;
