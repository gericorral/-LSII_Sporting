import type { VideoSummary } from '../useAllVideos';

type VideoGridProps = {
  videos: VideoSummary[];
};

const VideoGrid = ({ videos }: VideoGridProps) => {
  return (
    <ul className="video-grid">
      {videos.map((video) => (
        <li className="video-card" key={video.id}>
          <img src={video.thumbnailUrl} alt={`Thumbnail for ${video.title}`} />
          <div className="video-card-content">
            <h3>{video.title}</h3>
            <p>{video.description || 'No description available.'}</p>
            {video.duration !== null && <span>{video.duration}s</span>}
          </div>
        </li>
      ))}
    </ul>
  );
};

export default VideoGrid;
