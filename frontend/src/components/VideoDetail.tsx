import type { VideoSummary } from '../useAllVideos';
import { getEnv, resolveMediaUrl } from '../utils/Env';
import { useState } from 'react';
import axios from 'axios';
import CommentsSection from './CommentsSection';
import { useVideoRating } from '../useVideoRating';
import { useVideoFavorite } from '../useVideoFavorite';

type VideoDetailProps = {
  video: VideoSummary;
  canEdit?: boolean;
  onUpdated?: () => Promise<void>;
  loggedUser?: { username: string } | null;
};

const VideoDetail = ({ video, canEdit = false, onUpdated, loggedUser = null }: VideoDetailProps) => {
  const {
    rating,
    loading: ratingLoading,
    saving: ratingSaving,
    message: ratingMessage,
    saveRating,
  } = useVideoRating(video.id, Boolean(loggedUser));
  const {
    favorited,
    state: favoriteState,
    message: favoriteMessage,
    toggleFavorite,
  } = useVideoFavorite(video.id, Boolean(loggedUser));
  const [viewRegistered, setViewRegistered] = useState(false);
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
      await axios.put(
        `${getEnv().API_BASE_URL}/videos/${encodeURIComponent(video.id)}`,
        {
          title,
          description,
          tags: tags
            .split(',')
            .map((tag) => tag.trim())
            .filter(Boolean),
          category,
        },
        { withCredentials: true }
      );
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

  const registerView = async () => {
    if (viewRegistered) return;
    setViewRegistered(true);
    try {
      await axios.post(`${getEnv().API_BASE_URL}/videos/${encodeURIComponent(video.id)}/views`);
    } catch {
      setViewRegistered(false);
    }
  };

  return (
    <article className="video-detail">
      <a href="#">&larr; Back to videos</a>
      <h2>{video.title}</h2>
      <video
        controls
        poster={resolveMediaUrl(video.thumbnailUrl)}
        src={resolveMediaUrl(video.videoUrl)}
        onPlay={registerView}
      >
        Your browser does not support video playback.
      </video>
      <section className="video-favorite" aria-label="Favorite video">
        <button
          type="button"
          onClick={toggleFavorite}
          disabled={!loggedUser || favoriteState === 'loading' || favoriteState === 'saving'}
          aria-pressed={favorited}
        >
          {favorited ? 'Remove from favorites' : 'Add to favorites'}
        </button>
        {!loggedUser && <p>Log in to save this video as a favorite.</p>}
        {favoriteMessage && <p role="alert">{favoriteMessage}</p>}
      </section>
      {canEdit && !editing && (
        <button className="edit-video-button" type="button" onClick={() => setEditing(true)}>
          Edit metadata
        </button>
      )}
      {editing ? (
        <div className="video-edit-form">
          <label htmlFor="edit-video-title">Title</label>
          <input id="edit-video-title" value={title} onChange={(event) => setTitle(event.target.value)} />
          <label htmlFor="edit-video-description">Description</label>
          <textarea
            id="edit-video-description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
          <label htmlFor="edit-video-tags">Tags</label>
          <input
            id="edit-video-tags"
            value={tags}
            onChange={(event) => setTags(event.target.value)}
            placeholder="music, live, tutorial"
          />
          <label htmlFor="edit-video-category">Category</label>
          <input id="edit-video-category" value={category} onChange={(event) => setCategory(event.target.value)} />
          {error && <p role="alert">{error}</p>}
          <div className="video-edit-actions">
            <button type="button" onClick={save} disabled={saving}>
              {saving ? 'Saving...' : 'Save changes'}
            </button>
            <button type="button" onClick={() => setEditing(false)}>
              Cancel
            </button>
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
      <section className="video-rating" aria-label="Video rating">
        <div className="video-rating-heading">
          <div>
            <p className="eyebrow">Community score</p>
            <h3>Rate this video</h3>
          </div>
          <span className="rating-score">
            {typeof rating?.averageRating === 'number' ? rating.averageRating.toFixed(2) : '0.00'}
            <small>/ 5</small>
          </span>
        </div>
        {ratingLoading ? (
          <p>Loading rating...</p>
        ) : (
          <>
            <p className="rating-count">
              {rating?.ratingCount || 0} {rating?.ratingCount === 1 ? 'rating' : 'ratings'}
            </p>
            <div className="rating-actions" role="group" aria-label="Choose a rating from 1 to 5 stars">
              {[1, 2, 3, 4, 5].map((value) => (
                <button
                  key={value}
                  type="button"
                  aria-label={`Rate ${value} star${value === 1 ? '' : 's'}`}
                  aria-pressed={rating?.userRating === value}
                  disabled={!loggedUser || ratingSaving}
                  onClick={() => saveRating(value)}
                >
                  <span aria-hidden="true">{rating?.userRating && value <= rating.userRating ? '★' : '☆'}</span>
                  <small>{value}</small>
                </button>
              ))}
            </div>
            {loggedUser && rating?.userRating && <p className="your-rating">Your rating: {rating.userRating}/5</p>}
            {!loggedUser && <p>Log in to rate this video.</p>}
            {ratingMessage && <p role="alert">{ratingMessage}</p>}
          </>
        )}
      </section>
      <CommentsSection videoId={video.id} loggedUser={loggedUser} />
    </article>
  );
};

export default VideoDetail;
