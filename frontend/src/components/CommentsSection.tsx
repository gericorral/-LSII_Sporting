import { FormEvent, useState } from 'react';
import axios, { AxiosError } from 'axios';
import { getEnv } from '../utils/Env';
import { Comment, useComments } from '../useComments';
import { useTranslation } from 'react-i18next';
import i18n from '../i18n';

type CommentsSectionProps = {
  videoId: string;
  loggedUser: { username: string } | null;
};

const MAX_COMMENT_LENGTH = 2000;

function parseCommentDate(value: Comment['createdAt']): Date | null {
  if (typeof value === 'string') {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
  }
  if (Array.isArray(value) && value.length >= 3 && value.every((part) => Number.isFinite(part))) {
    const [year, month, day, hour = 0, minute = 0, second = 0, nanoseconds = 0] = value;
    const milliseconds = Math.floor(nanoseconds / 1_000_000);
    const date = new Date(Date.UTC(year, month - 1, day, hour, minute, second, milliseconds));
    if (
      date.getUTCFullYear() !== year ||
      date.getUTCMonth() !== month - 1 ||
      date.getUTCDate() !== day ||
      date.getUTCHours() !== hour ||
      date.getUTCMinutes() !== minute ||
      date.getUTCSeconds() !== second
    ) {
      return null;
    }
    return Number.isNaN(date.getTime()) ? null : date;
  }
  return null;
}

function formatCommentDate(value: Comment['createdAt']): { dateTime?: string; label: string } {
  const date = parseCommentDate(value);
  if (!date) {
    return { label: i18n.t('comments.dateUnavailable') };
  }
  return { dateTime: date.toISOString(), label: date.toLocaleString() };
}

export default function CommentsSection({ videoId, loggedUser }: CommentsSectionProps) {
  const { t } = useTranslation();
  const { comments, loading, message, hasNext, loadMore, addComment, updateLike } = useComments(videoId);
  const [content, setContent] = useState('');
  const [replyTo, setReplyTo] = useState<string | null>(null);
  const [replyContent, setReplyContent] = useState('');
  const [submitMessage, setSubmitMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const normalizedContent = content.trim();
    if (!normalizedContent) {
      setSubmitMessage(t('comments.empty'));
      return;
    }
    if (normalizedContent.length > MAX_COMMENT_LENGTH) {
      setSubmitMessage(t('comments.tooLong'));
      return;
    }
    setSubmitMessage('');
    setSubmitting(true);
    try {
      const response = await axios.post<Comment>(
        `${getEnv().API_BASE_URL}/videos/${encodeURIComponent(videoId)}/comments`,
        { content: normalizedContent },
        { withCredentials: true }
      );
      addComment(response.data);
      setContent('');
    } catch (error: unknown) {
      setSubmitMessage(t('comments.errorPosting', { error: (error as AxiosError).message }));
    } finally {
      setSubmitting(false);
    }
  };

  const submitReply = async (parentCommentId: string) => {
    const normalizedContent = replyContent.trim();
    if (!normalizedContent) {
      setSubmitMessage(t('comments.replyEmpty'));
      return;
    }
    if (normalizedContent.length > MAX_COMMENT_LENGTH) {
      setSubmitMessage(t('comments.replyTooLong'));
      return;
    }
    setSubmitMessage('');
    setSubmitting(true);
    try {
      const response = await axios.post<Comment>(
        `${getEnv().API_BASE_URL}/videos/${encodeURIComponent(videoId)}/comments`,
        { content: normalizedContent, parentCommentId },
        { withCredentials: true }
      );
      addComment(response.data);
      setReplyContent('');
      setReplyTo(null);
    } catch (error: unknown) {
      setSubmitMessage(t('comments.errorReplying', { error: (error as AxiosError).message }));
    } finally {
      setSubmitting(false);
    }
  };

  const renderComment = (comment: Comment) => {
    const replies = comments.filter((item) => item.parentCommentId === comment.id);
    return (
      <li key={comment.id} className={comment.parentCommentId ? 'comment comment-reply' : 'comment'}>
        <strong>{comment.authorName}</strong>
        {(() => {
          const formattedDate = formatCommentDate(comment.createdAt);
          return <time dateTime={formattedDate.dateTime}>{formattedDate.label}</time>;
        })()}
        <p>{comment.content}</p>
        <div className="comment-actions">
          {loggedUser && (
            <>
              <button type="button" onClick={() => updateLike(comment.id)}>
                {comment.likedByCurrentUser ? t('comments.unlike') : t('comments.like')} ({comment.likeCount || 0})
              </button>
              <button
                type="button"
                onClick={() => {
                  setReplyTo(replyTo === comment.id ? null : comment.id);
                  setSubmitMessage('');
                }}
              >
                {t('comments.reply')}
              </button>
            </>
          )}
        </div>
        {replyTo === comment.id && (
          <form
            onSubmit={(event) => {
              event.preventDefault();
              submitReply(comment.id);
            }}
          >
            <label htmlFor={`reply-${comment.id}`}>{t('comments.reply')}</label>
            <textarea
              id={`reply-${comment.id}`}
              value={replyContent}
              maxLength={MAX_COMMENT_LENGTH}
              onChange={(event) => setReplyContent(event.target.value)}
            />
            <button type="submit" disabled={submitting}>
              {submitting ? t('comments.posting') : t('comments.postReply')}
            </button>
          </form>
        )}
        {replies.length > 0 && <ul className="comments-list">{replies.map(renderComment)}</ul>}
      </li>
    );
  };

  const rootComments = comments.filter((comment) => !comment.parentCommentId);

  return (
    <section className="comments-section" aria-labelledby="comments-heading">
      <h3 id="comments-heading">{t('comments.title')}</h3>
      {loggedUser ? (
        <form onSubmit={submit} className="comment-form">
          <label htmlFor="comment-content">{t('comments.add')}</label>
          <textarea
            id="comment-content"
            value={content}
            maxLength={MAX_COMMENT_LENGTH}
            onChange={(event) => setContent(event.target.value)}
            placeholder={t('comments.placeholder')}
          />
          {submitMessage && <p role="alert">{submitMessage}</p>}
          <button type="submit" disabled={submitting}>
            {submitting ? t('comments.posting') : t('comments.post')}
          </button>
        </form>
      ) : (
        <p>
          <a href="#login">{t('login')}</a> {t('comments.loginToComment')}
        </p>
      )}
      {loading === 'loading' && <p role="status">{t('comments.loading')}</p>}
      {loading === 'error' && <p role="alert">{message}</p>}
      {loading === 'success' && comments.length === 0 && <p>{t('comments.noComments')}</p>}
      <ul className="comments-list">{rootComments.map(renderComment)}</ul>
      {hasNext && (
        <button type="button" className="load-more-button" onClick={loadMore} disabled={loading === 'loading'}>
          {t('comments.loadMore')}
        </button>
      )}
    </section>
  );
}
