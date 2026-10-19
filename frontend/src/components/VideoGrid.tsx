import type { VideoSummary } from '../useAllVideos';
import { resolveMediaUrl } from '../utils/Env';

type VideoGridProps = {
  videos: VideoSummary[];
};

const VideoGrid = ({ videos }: VideoGridProps) => {
  return (
    <ul className="video-grid">
      {videos.map((video) => (
        <li className="video-card" key={video.id}>
          <a href={`#video/${encodeURIComponent(video.id)}`}>
            <img src={resolveMediaUrl(video.thumbnailUrl)} alt={`Thumbnail for ${video.title}`} />
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
