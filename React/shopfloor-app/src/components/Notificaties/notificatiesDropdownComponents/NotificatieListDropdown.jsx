import { Link } from 'react-router-dom';
import NotificatieDropdown from './NotificatieDropdown';
import { useTranslation } from 'react-i18next';

export default function NotificatieListDropdown({ notificaties, toggleDropdown }) {
  const {t} = useTranslation();
  if (!notificaties) {
    return (
      <div className="bg-white shadow-md rounded p-4 w-64">
        <p>{t('no-notifications')}</p>
      </div>
    );
  }
  
  const ongelezenNotificaties = notificaties.items.filter(
    (notificatie) => !notificatie.isread);

  return (
    <div className='bg-white shadow-md rounded'>
      <div className="w-128 max-h-80 overflow-y-auto p-2">
        <div className="p-3">
          <p className="font-bold">{t('notifications')}</p>
        </div>
        <div className='pl-4 pr-4 pb-2'>
          {ongelezenNotificaties.length === 0 ? (
            <p className="text-gray-500">{t('no-unread-notifications')}</p>
          ) : (
            ongelezenNotificaties.map((notificatie) => (
              <NotificatieDropdown 
                key={notificatie.id} id={notificatie.id} 
                message={notificatie.message} time={notificatie.time} />
            ))
          )}
        </div>
      </div>
      <div className='pl-4 pb-2'>
        <Link to='/notificaties' onClick={() => toggleDropdown()}>
          <p className='mt-2 font-bold text-sm'>{t('see-all-notifications')}</p>
        </Link>
      </div>
    </div>
  );
}
  