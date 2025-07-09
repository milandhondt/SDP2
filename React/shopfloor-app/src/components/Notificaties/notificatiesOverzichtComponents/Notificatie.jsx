import useSWRMutation from 'swr/mutation';
import { save } from '../../../api';
import { useTranslation } from 'react-i18next';

export default function Notificatie({id, time, message, isread}){

  const {t} = useTranslation();

  const {
    trigger: markAsRead,
  } = useSWRMutation('notificaties', save, {method: 'PUT'}); 

  const handleMarkAsRead = async () => {
    await markAsRead({id, time, message, isread: true});
  };

  const tijdstipString = new Date(time).toLocaleDateString();

  return (
    <div className='border rounded flex flex-row gap-3 p-3 mb-6 mr-6'>
      <div className='flex items-center'>
        <p>{id}</p>
      </div>
      <div className='flex flex-col w-full'>
        <p className='font-semibold'>{t('date')}: {tijdstipString}</p>
        <p>{message}</p>
        {
          isread ? '' : <p className='font-semibold hover:cursor-pointer' 
            onClick={handleMarkAsRead}>{t('mark-as-read')}</p>
        }
      </div>
    </div>
  );
}