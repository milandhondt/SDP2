import { useTranslation } from 'react-i18next';

const NotFound = () => {
  const {t} = useTranslation();
  return (
    <div className="flex flex-col items-center justify-center mt-30 text-center p-6">
      <h1 className="text-4xl font-bold text-gray-800 mb-4">{t('Pagina niet gevonden')}</h1>
      <p className="text-lg text-gray-600 mb-6">{t('page-not-found-p')}</p>
      <a
        href="/"
        className="px-5 py-3 bg-red-500 text-white rounded-lg 
        shadow-md hover:bg-red-700 transition transform duration-300"
      >
        {t('go-back-home')}
      </a>
    </div>
  );
};

export default NotFound;
