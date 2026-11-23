# Require upload thumbnails

Update the video upload flow so users must provide a common image thumbnail. Validate
the image in the frontend and backend, pass it through the upload use case and
publication port, store it atomically beside the MP4 and metadata, and persist its
actual MIME type in PostgreSQL asset metadata. Preserve existing catalog behavior and
update focused tests and study documentation.
