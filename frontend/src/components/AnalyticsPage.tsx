import { useAnalytics } from '../useAnalytics';

type AnalyticsPageProps = {
  onBack: () => void;
};

export default function AnalyticsPage({ onBack }: AnalyticsPageProps) {
  const { value, loading, message } = useAnalytics(true);

  return (
    <section className="analytics-panel" aria-labelledby="analytics-title">
      <button type="button" onClick={onBack}>
        Back to videos
      </button>
      <div className="analytics-heading">
        <p className="registration-eyebrow">MY CONTENT</p>
        <h2 id="analytics-title">Video analytics</h2>
        <p>Understand how your videos are performing.</p>
      </div>
      {loading === 'loading' && <p role="status">Loading analytics...</p>}
      {loading === 'error' && <p role="alert">{message}</p>}
      {loading === 'success' && value.length === 0 && <p role="status">You have not uploaded any videos yet.</p>}
      {loading === 'success' && value.length > 0 && (
        <div className="analytics-list">
          {value.map((video) => (
            <article className="analytics-card" key={video.videoId}>
              <h3>{video.title}</h3>
              <dl>
                <div>
                  <dt>Views</dt>
                  <dd>{video.views}</dd>
                </div>
                <div>
                  <dt>Comments</dt>
                  <dd>{video.comments}</dd>
                </div>
                <div>
                  <dt>Likes</dt>
                  <dd>{video.likes}</dd>
                </div>
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
