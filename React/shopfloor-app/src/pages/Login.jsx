import { useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { FormProvider, useForm } from 'react-hook-form';
import LabelInputLogin from '../components/LabelInputLogin.jsx';
import { useAuth } from '../contexts/auth';
import Error from '../components/Error';
import Loader from '../components/Loader.jsx';
import { useTranslation } from 'react-i18next';

const Login = () => {
  const { search } = useLocation();

  const params = new URLSearchParams(search);
  const isLoggedOut = params.get('logout') === 'success';

  const { error, loading, login } = useAuth();
  const navigate = useNavigate();

  const methods = useForm();
  const { handleSubmit } = methods;

  const handleLogin = useCallback(
    async ({ email, password }) => {
      const loggedIn = await login(email, password);
      if (loggedIn) {
        localStorage.setItem('loginTime', new Date());
        const params = new URLSearchParams(search);
        navigate(params.get('redirect') || '/', { replace: true });
      }
    },
    [login, navigate, search],
  );

  const {t} = useTranslation();

  return (
    <div className="flex flex-col items-center min-h-screen bg-cover bg-center"
      style={{ backgroundImage: 'url(\'/login_achtergrond.svg\')' }}>
      <img
        src='/delaware_logo.png'
        alt="Delaware Logo"
        className="w-60 md:w-70 mt-15 mb-3 drop-shadow-lg"
      />
      <div className="flex flex-col md:flex-row bg-white bg-opacity-90
       p-8 rounded-lg shadow-lg w-11/12 max-w-4xl">
        <div className="w-full md:w-1/2 p-6">
          <h2 className="text-2xl font-bold mb-4">{t('welcome').toLocaleUpperCase()}!</h2>
          {isLoggedOut && (
            <div className="mb-4 p-3 text-green-800 bg-green-100 border border-green-300 rounded">
              {t('logout-succesful')}
            </div>
          )}

          <p className="text-gray-600 text-sm mb-4">
            {t('no-account')}
          </p>

          <FormProvider {...methods}>
            <form className="space-y-4" onSubmit={handleSubmit(handleLogin)}>
              <div>
                <LabelInputLogin
                  label={t('email') + ':'}
                  name="email"
                  type="email"
                  placeholder={t('email-placeholder')}
                  validationRules={{ required: t('email-required') }}
                  data-cy="loginEmail"
                />
              </div>
              <div>
                <LabelInputLogin
                  label={t('password') + ':'}
                  name="password"
                  type="password"
                  placeholder="●●●●●●●●"
                  validationRules={{ required: t('password-required') }}
                  data-cy="loginWachtwoord"
                />
              </div>
              <Error error={error} />
              <button
                type="submit"
                className="disabled:bg-red-400 enabled:hover:cursor-pointer 
                  w-full bg-red-500 text-white py-2 rounded 
                  hover:bg-red-600 transition mt-5"
                disabled={loading}
                data-cy="loginSubmitButton"
              >
                {loading ? <Loader /> : t('login')}
              </button>
            </form>
          </FormProvider>
        </div>

        <div className="hidden md:block w-1/2">
          <img
            src="/login_groepsfoto.jpg"
            alt="Groepsfoto"
            className="rounded-r-lg object-cover w-full h-full"
          />
        </div>
      </div>
    </div>
  );
};

export default Login;
