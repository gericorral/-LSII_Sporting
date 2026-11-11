import type { VideoSummary } from '../useAllVideos';
import { getEnv, resolveMediaUrl } from '../utils/Env';
import { useEffect, useState } from 'react';
import axios from 'axios';
import CommentsSection from './CommentsSection';
import { useVideoRating } from '../useVideoRating';
import { useVideoFavorite } from '../useVideoFavorite';
import { useTranslation } from 'react-i18next';

type VideoDetailProps = {
  video: VideoSummary;
  canEdit?: boolean;
  onUpdated?: () => Promise<void>;
  loggedUser?: { id?: string; username: string } | null;
};

const VideoDetail = ({ video, canEdit = false, onUpdated, loggedUser = null }: VideoDetailProps) => {
  const { t } = useTranslation();
  const [author, setAuthor] = useState<{
    userId: string;
    username: string;
    displayName: string | null;
    avatarPath: string | null;
  } | null>(null);
  const [subscribed, setSubscribed] = useState(false);
  const [subscriptionSaving, setSubscriptionSaving] = useState(false);
  const [subscriptionMessage, setSubscriptionMessage] = useState('');
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

  useEffect(() => {
    if (!video.ownerId) return;
    setAuthor({
      userId: video.ownerId,
      username: video.ownerUsername || t('video.unknownAuthor'),
      displayName: null,
      avatarPath: null,
    });
    const ownerId = encodeURIComponent(video.ownerId);
    axios
      .get(`${getEnv().API_BASE_URL}/profiles/${ownerId}`, { withCredentials: true })
      .then((response) => setAuthor(response.data))
      .catch(() => undefined);
    if (loggedUser && loggedUser.id !== video.ownerId) {
      axios
        .get<{ subscribed: boolean }>(`${getEnv().API_BASE_URL}/profiles/${ownerId}/subscription`, {
          withCredentials: true,
        })
        .then((response) => setSubscribed(response.data.subscribed))
        .catch(() => undefined);
    }
  }, [loggedUser, t, video.ownerId, video.ownerUsername]);

  const changeSubscription = async () => {
    if (!video.ownerId || !loggedUser || loggedUser.id === video.ownerId) return;
    setSubscriptionSaving(true);
    setSubscriptionMessage('');
    try {
      const ownerId = encodeURIComponent(video.ownerId);
      const response = subscribed
        ? await axios.delete(`${getEnv().API_BASE_URL}/profiles/${ownerId}/subscription`, { withCredentials: true })
        : await axios.put(`${getEnv().API_BASE_URL}/profiles/${ownerId}/subscription`, {}, { withCredentials: true });
      setSubscribed(response.data.subscribed);
    } catch (error: unknown) {
      if (axios.isAxiosError<{ message?: string }>(error) && error.response?.data?.message) {
        setSubscriptionMessage(error.response.data.message);
      } else {
        setSubscriptionMessage(t('video.loginSubscribe'));
      }
    } finally {
      setSubscriptionSaving(false);
    }
  };

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
        setError(saveError.response?.data?.message || t('video.updateFailed'));
      } else {
        setError(t('video.updateFailed'));
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
      <a href="#">&larr; {t('backToVideos')}</a>
      <h2>{video.title}</h2>
      <video
        controls
        poster={resolveMediaUrl(video.thumbnailUrl)}
        src={resolveMediaUrl(video.videoUrl)}
        onPlay={registerView}
      >
        {t('video.playbackUnsupported')}
      </video>
      {author && (
        <section className="video-author" aria-label={t('video.author')}>
          <a href={`#profile/${encodeURIComponent(author.userId)}`} className="video-author-profile">
            {author.avatarPath ? (
              <img src={author.avatarPath} alt={`${author.username}'s avatar`} />
            ) : (
              <span aria-hidden="true">{author.username.charAt(0).toUpperCase()}</span>
            )}
            <strong>{author.displayName || author.username}</strong>
          </a>
          {loggedUser?.id !== author.userId && (
            <button type="button" onClick={changeSubscription} disabled={!loggedUser || subscriptionSaving}>
              {subscriptionSaving ? t('video.updating') : subscribed ? t('video.unsubscribe') : t('video.subscribe')}
            </button>
          )}
          {!loggedUser && <p>{t('video.loginSubscribe')}</p>}
          {subscriptionMessage && <p role="alert">{subscriptionMessage}</p>}
        </section>
      )}
      <section className="video-favorite" aria-label={t('video.favorite')}>
        <button
          type="button"
          onClick={toggleFavorite}
          disabled={!loggedUser || favoriteState === 'loading' || favoriteState === 'saving'}
          aria-pressed={favorited}
        >
          {favorited ? t('video.removeFavorite') : t('video.addFavorite')}
        </button>
        {!loggedUser && <p>{t('video.loginFavorite')}</p>}
        {favoriteMessage && <p role="alert">{favoriteMessage}</p>}
      </section>
      {canEdit && !editing && (
        <button className="edit-video-button" type="button" onClick={() => setEditing(true)}>
          {t('video.edit')}
        </button>
      )}
      {editing ? (
        <div className="video-edit-form">
          <label htmlFor="edit-video-title">{t('upload.titleLabel')}</label>
          <input id="edit-video-title" value={title} onChange={(event) => setTitle(event.target.value)} />
          <label htmlFor="edit-video-description">{t('video.description')}</label>
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
            placeholder={t('video.tagsPlaceholder')}
          />
          <label htmlFor="edit-video-category">{t('video.category')}</label>
          <input id="edit-video-category" value={category} onChange={(event) => setCategory(event.target.value)} />
          {error && <p role="alert">{error}</p>}
          <div className="video-edit-actions">
            <button type="button" onClick={save} disabled={saving}>
              {saving ? t('video.saving') : t('video.save')}
            </button>
            <button type="button" onClick={() => setEditing(false)}>
              {t('video.cancel')}
            </button>
          </div>
        </div>
      ) : (
        <>
          <p>{video.description || t('video.noDescription')}</p>
          {video.category && <span className="video-category">{video.category}</span>}
          {video.tags?.length ? <p className="video-tags">{video.tags.map((tag) => `#${tag}`).join(' ')}</p> : null}
        </>
      )}
      {video.duration !== null && <span>{video.duration}s</span>}
      <section className="video-rating" aria-label={t('video.rate')}>
        <div className="video-rating-heading">
          <div>
            <p className="eyebrow">{t('video.communityScore')}</p>
            <h3>{t('video.rate')}</h3>
          </div>
          <span className="rating-score">
            {typeof rating?.averageRating === 'number' ? rating.averageRating.toFixed(2) : '0.00'}
            <small>/ 5</small>
          </span>
        </div>
        {ratingLoading ? (
          <p>{t('video.loadingRating')}</p>
        ) : (
          <>
            <p className="rating-count">
              {rating?.ratingCount || 0} {rating?.ratingCount === 1 ? t('video.rating') : t('video.ratings')}
            </p>
            <div className="rating-actions" role="group" aria-label={t('video.chooseRating')}>
              {[1, 2, 3, 4, 5].map((value) => (
                <button
                  key={value}
                  type="button"
                  aria-label={value === 1 ? t('video.rateStar', { value }) : t('video.rateStars', { value })}
                  aria-pressed={rating?.userRating === value}
                  disabled={!loggedUser || ratingSaving}
                  onClick={() => saveRating(value)}
                >
                  <span aria-hidden="true">{rating?.userRating && value <= rating.userRating ? '★' : '☆'}</span>
                  <small>{value}</small>
                </button>
              ))}
            </div>
            {loggedUser && rating?.userRating && (
              <p className="your-rating">{t('video.yourRating', { value: rating.userRating })}</p>
            )}
            {!loggedUser && <p>{t('video.loginRating')}</p>}
            {ratingMessage && <p role="alert">{ratingMessage}</p>}
          </>
        )}
      </section>
      <CommentsSection videoId={video.id} loggedUser={loggedUser} />
    </article>
  );
};

export default VideoDetail;
