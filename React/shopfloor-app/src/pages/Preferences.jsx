import { useTranslation } from 'react-i18next';

export default function Preferences() {
  const { i18n, t } = useTranslation();

  const changeLanguage = (lng) => {
    i18n.changeLanguage(lng);
  };

  return (
    <div className="p-4">
      <h2 className="text-xl font-semibold mb-4">{t('preferences')}</h2>
      
      <div className="mb-2">
        <label className="block mb-1 font-medium">{t('language')}</label>
        <select
          value={i18n.language}
          onChange={(e) => changeLanguage(e.target.value)}
          className="border border-gray-300 rounded px-3 py-2"
        >
          <option value="en">English</option>
          <option value="nl">Nederlands</option>
        </select>
      </div>
    </div>
  );
}
