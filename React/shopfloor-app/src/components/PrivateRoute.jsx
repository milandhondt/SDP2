import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/auth';
import { useTranslation } from 'react-i18next';

export default function PrivateRoute() {
  const { ready, isAuthed } = useAuth();
  const { pathname } = useLocation();
  const {t} = useTranslation();
  
  if (!ready) {
    return (
      <div className='container'>
        <div className='row'>
          <div className='col-12'>
            <h1>{t('loading')}...</h1>
            <p>
              {t('privateroute-please-wait')}
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (!isAuthed) {
    return <Navigate replace to={`/login?redirect=${pathname}`} />;
  }

  return <Outlet />;
}