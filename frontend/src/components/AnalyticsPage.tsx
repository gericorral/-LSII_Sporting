import { useAnalytics } from '../useAnalytics';
import { useTranslation } from 'react-i18next';

type AnalyticsPageProps = {
  onBack: () => void;
};

export default function AnalyticsPage({ onBack }: AnalyticsPageProps) {
  const { t } = useTranslation();
  const { value, loading, message } = useAnalytics(true);

  return (
    <section className="analytics-panel" aria-labelledby="analytics-title">
      <button type="button" onClick={onBack}>
        {t('backToVideos')}
      </button>
      <div className="analytics-heading">
        <p className="registration-eyebrow">{t('pages.analyticsEyebrow')}</p>
        <h2 id="analytics-title">{t('pages.analyticsTitle')}</h2>
        <p>{t('pages.analyticsDescription')}</p>
      </div>
      {loading === 'loading' && <p aria-live="polite">{t('pages.loadingAnalytics')}</p>}
      {loading === 'error' && <p role="alert">{message}</p>}
      {loading === 'success' && value.length === 0 && <p role="status">{t('pages.noUploads')}</p>}
      {loading === 'success' && value.length > 0 && (
        <div className="analytics-list">
          {value.map((video) => (
            <article className="analytics-card" key={video.videoId}>
              <h3>{video.title}</h3>
              <dl>
                <div>
                  <dt>{t('pages.views')}</dt>
                  <dd>{video.views}</dd>
                </div>
                <div>
                  <dt>{t('pages.comments')}</dt>
                  <dd>{video.comments}</dd>
                </div>
                <div>
                  <dt>{t('pages.likes')}</dt>
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
