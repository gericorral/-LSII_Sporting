import type { VideoSummary } from '../useAllVideos';
import { resolveMediaUrl } from '../utils/Env';

type VideoDetailProps = {
  video: VideoSummary;
};

const VideoDetail = ({ video }: VideoDetailProps) => (
  <article className="video-detail">
    <a href="#">&larr; Back to videos</a>
    <h2>{video.title}</h2>
    <video controls poster={resolveMediaUrl(video.thumbnailUrl)} src={resolveMediaUrl(video.videoUrl)}>
      Your browser does not support video playback.
    </video>
    <p>{video.description || 'No description available.'}</p>
    {video.duration !== null && <span>{video.duration}s</span>}
  </article>
);

export default VideoDetail;
