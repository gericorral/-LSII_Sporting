import type { VideoSummary } from '../useAllVideos';
import { resolveMediaUrl } from '../utils/Env';
import { useTranslation } from 'react-i18next';

type VideoGridProps = {
  videos: VideoSummary[];
};

const VideoGrid = ({ videos }: VideoGridProps) => {
  const { t } = useTranslation();
  return (
    <ul className="video-grid">
      {videos.map((video) => (
        <li className="video-card" key={video.id}>
          <a href={`#video/${encodeURIComponent(video.id)}`}>
            <img src={resolveMediaUrl(video.thumbnailUrl)} alt={t('thumbnailFor', { title: video.title })} />
            <div className="video-card-content">
              <h3>{video.title}</h3>
              {video.duration !== null && <span>{video.duration}s</span>}
            </div>
          </a>
        </li>
      ))}
    </ul>
  );
};

export default VideoGrid;
