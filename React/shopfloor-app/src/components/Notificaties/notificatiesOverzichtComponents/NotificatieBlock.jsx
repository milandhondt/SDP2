import Notificatie from './Notificatie';
import { IoIosArrowDown, IoIosArrowUp } from 'react-icons/io';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

export default function NotificatieBlock({notificaties, type}){

  const {t} = useTranslation();

  const [notifsOpen, setNotifsOpen] = useState(false);

  const handleNotifsOpen = () => {
    setNotifsOpen(!notifsOpen);
  };

  if(notificaties.length === 0){
    return (
      <div className='border border-gray-500 rounded mb-4 pl-8'>
        <h2 className='text-2xl mb-4 mt-4 font-semibold w-full'>{t('none')}
          {new String(` ${type} `).toLocaleLowerCase()} 
          {t('notifications').toLocaleLowerCase()}</h2>
      </div>
    );
  }

  return (
    <div className='border border-gray-500 rounded mb-4'>
      <div className="pl-8">
        <div className='flex flex-row items-center' onClick={handleNotifsOpen}>
          <h2 className="items-center text-2xl mb-4 mt-4 font-semibold w-full">
            {type} {t('messages')} ({notificaties.length}) 
          </h2>
          <div className='text-3xl mr-4'>
            {notifsOpen ? <IoIosArrowUp/> : <IoIosArrowDown/> }
          </div>
        </div>
        {notifsOpen ? <div>
          {notificaties.map((notificatie) => 
            <Notificatie 
              key={notificatie.id} id={notificatie.id} time={notificatie.time} message={notificatie.message} 
              gelezen={notificatie.isread}
            />)}
        </div> : ''}
      </div>
    </div>
  );
}