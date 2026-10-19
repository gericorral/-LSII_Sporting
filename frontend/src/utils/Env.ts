export const getEnv = () => {
  const { VITE_API_DOMAIN, VITE_MEDIA_DOMAIN, ...otherViteConfig } = import.meta.env;

  return {
    API_BASE_URL: `${VITE_API_DOMAIN}/api`,
    MEDIA_BASE_URL: `${VITE_MEDIA_DOMAIN}/media`,
    __vite__: otherViteConfig,
  };
};

export const resolveMediaUrl = (url: string) => {
  if (/^https?:\/\//i.test(url)) {
    return url;
  }

  const mediaBaseUrl = getEnv().MEDIA_BASE_URL.replace(/\/$/, '');
  const mediaPath = url.replace(/^\/media\/?/, '');
  return `${mediaBaseUrl}/${mediaPath}`;
};
