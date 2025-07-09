import { useTranslation } from 'react-i18next';
import NotificatieBlock from './NotificatieBlock';

export default function NotificatieList({notificaties}){

  const {t} = useTranslation();

  if(!notificaties){
    return (
      <div className="grid justify-center">Geen notificaties gevonden!</div>
    );
  }

  const ongelezenNotificaties = notificaties.items.filter((notificatie) => !notificatie.isread);
  const loginTime = new Date(localStorage.getItem('loginTime'));

  const nieuweNotificaties = ongelezenNotificaties.filter(
    (notificatie) => new Date(notificatie.time) >= loginTime);

  const overigeOngelezenNotificaties = ongelezenNotificaties.filter(
    (notificatie) =>
      !nieuweNotificaties.find((nieuw) => nieuw.id === notificatie.id),
  );

  const gelezenNotificaties = notificaties.items.filter((notificatie) => notificatie.isread);
  return (
    <div className="w-full">
      <NotificatieBlock notificaties={nieuweNotificaties} type={t('new')}/>
      <NotificatieBlock notificaties={overigeOngelezenNotificaties} type={t('unread')}/>
      <NotificatieBlock notificaties={gelezenNotificaties} type={t('read')}/>
    </div>
  );
}