import useSWRMutation from 'swr/mutation';
import { save } from '../../../api';
import { useTranslation } from 'react-i18next';

export default function NotificatieDropdown({id, message, time}){
  const {t} = useTranslation();

  const {
    trigger: markAsRead,
  } = useSWRMutation('notificaties', save, {method: 'PUT'}); 

  const handleMarkAsRead = async () => {
    await markAsRead({id, time, message, isread: true});
  };

  return(
    <div
      className="flex flex-col p-4 mb-1 bg-gray-100 rounded hover:bg-gray-200 transition"
      key={id}
    >
      <span className="text-sm font-medium">{message}</span>
      <span className="text-xs text-gray-500">{new Date(time).toLocaleDateString()}</span>
      <span className="hover:text-red-500 hover:cursor-pointer text-xs hover:text-sm transition-all" 
        onClick={handleMarkAsRead}>{t('mark-as-read')}
      </span>
    </div>
  );
}