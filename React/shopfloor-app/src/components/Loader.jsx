import { useTranslation } from 'react-i18next';
import ClipLoader from 'react-spinners/ClipLoader';

export default function Loader() {
  const {t} = useTranslation();
  return (
    <div className="flex justify-center gap-1">
      {t('loading')} <ClipLoader color="white" size="25px" />
    </div>
  );
}