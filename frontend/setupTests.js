import '@testing-library/jest-dom';

import { enableMocks } from 'jest-fetch-mock';

jest.mock('./src/utils/Env', () => ({
  getEnv: () => ({
    API_BASE_URL: 'other',
    MEDIA_BASE_URL: '/media',
  }),
  resolveMediaUrl: (url) => url,
}));

enableMocks();
require('./src/i18n').default;
const i18n = require('./src/i18n').default;

beforeEach(() => {
  i18n.changeLanguage('en');
  window.localStorage.removeItem('protube-language');
});
