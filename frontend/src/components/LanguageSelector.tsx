import { useTranslation } from 'react-i18next';
import i18n from '../i18n';

export default function LanguageSelector() {
  const { t } = useTranslation();
  return (
    <label className="language-selector">
      <span>{t('language')}</span>
      <select
        aria-label={t('language')}
        value={i18n.language}
        onChange={(event) => i18n.changeLanguage(event.target.value)}
      >
        <option value="en">{t('english')}</option>
        <option value="es">{t('spanish')}</option>
      </select>
    </label>
  );
}
