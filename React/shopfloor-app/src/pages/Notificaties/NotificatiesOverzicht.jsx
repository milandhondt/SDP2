import { getAll } from '../../api';
import AsyncData from '../../components/AsyncData';
import useSWR from 'swr';
import NotificatieList from '../../components/Notificaties/notificatiesOverzichtComponents/NotificatieList';
import PageHeader from '../../components/genericComponents/PageHeader';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const NotificatiesOverzicht = () => {
  const navigate = useNavigate();
  const {t} = useTranslation();
  
  const {
    data: notificaties,
    loading,
    error,
  } = useSWR('notificaties', getAll);

  const handleBackClick = () => {
    navigate('/dashboard');
  };

  return (
    <>
      <PageHeader title={t('notifications')} onBackClick={handleBackClick}/>
    
      <AsyncData loading={loading} error={error}>
        <NotificatieList notificaties={notificaties} />
      </AsyncData>
    </>
  );
};

export default NotificatiesOverzicht;
