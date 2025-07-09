import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import useSWR from 'swr';
import { createSite, getAll, getById, updateSite } from '../../api';
import AsyncData from '../../components/AsyncData';
import PageHeader from '../../components/genericComponents/PageHeader';
import SiteInfoForm from '../../components/sites/SiteInfoForm';
import SuccessMessage from '../../components/genericComponents/SuccesMessage';
import { useAuth } from '../../contexts/auth';
import { useTranslation } from 'react-i18next';

export default function AddOrEditSite() {
  const { role } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();
  const isNewSite = !id || id === 'new';

  useEffect(() => {
    if (role !== 'MANAGER') {
      navigate('/not-found');
    }
  }, [role, navigate]);

  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    sitename: '',
    verantwoordelijke_id: '',
    status: 'ACTIEF',
  });
  const [successMessage, setSuccessMessage] = useState('');
  const [isFormDataInitialized, setIsFormDataInitialized] = useState(false);

  const filterVerantwoordelijken = (users) => {
    return Array.isArray(users)
      ? users.filter((user) => {
        try {
          if (typeof user.role === 'string') {
            const cleanedRol = user.role.replace(/\\/g, '');
            const parsedRol = cleanedRol.startsWith('"') && cleanedRol.endsWith('"')
              ? cleanedRol.slice(1, -1)
              : cleanedRol;
            return parsedRol === 'VERANTWOORDELIJKE';
          }
          return false;
        } catch (e) {
          console.error('Error parsing rol:', e);
          return false;
        }
      })
      : [];
  };

  // Fetch users data with useSWR
  const {
    data: usersData,
    error: usersError,
    loading: usersLoading,
  } = useSWR('/users', getAll);

  // Fetch site data if editing an existing site
  const {
    data: siteData,
    error: siteError,
    loading: siteLoading,
  } = useSWR(!isNewSite ? `/sites/${id}` : null, getById);

  // Process users data to filter verantwoordelijken
  const verantwoordelijken = usersData ? filterVerantwoordelijken(usersData.items) : [];

  // Set form data when site data is loaded - ONLY ONCE
  useEffect(() => {
    if (siteData && !isFormDataInitialized) {
      setFormData({
        sitename: siteData.sitename || '',
        verantwoordelijke_id: siteData.verantwoordelijke?.id || '',
        status: siteData.status || 'ACTIEF',
      });
      setIsFormDataInitialized(true);
    }
  }, [siteData, isFormDataInitialized]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const {t} = useTranslation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (isNewSite) {
        await createSite(formData);
        setSuccessMessage(t('site.added.success'));
      } else {
        await updateSite(id, formData);
        setSuccessMessage(t('site.updated.success'));
      }
    } catch (err) {
      console.error(`${isNewSite ? 'Creation' : 'Update'} failed:`, err);
      setError(`${t('site.error.1.1')} ${isNewSite ? 'toevoegen' : 'bijwerken'} ${t('site.error.1.2')}`);
    }
  };

  const handleOnClickBack = () => {
    navigate('/sites');
  };

  const pageTitle = isNewSite ? t('site.add-new') : t('site-edit', { sitename: formData.sitename });

  const isLoading = (!isNewSite && !siteData && !siteError)
    || (!usersData && !usersError) || siteLoading || usersLoading;
  const fetchError = usersError || siteError || error;

  return (
    <AsyncData loading={isLoading} error={fetchError}>
      <div className="p-2 md:p-4">
        <PageHeader
          title={isNewSite ? pageTitle : `Site | ${pageTitle}`}
          onBackClick={handleOnClickBack}
        />

        {successMessage && <SuccessMessage message={successMessage} />}

        <div className="bg-white p-3 md:p-6 border rounded">
          <h2 className="text-2xl md:text-3xl font-semibold mb-4 md:mb-6">{t('information')}</h2>

          <form onSubmit={handleSubmit} data-cy="site-form">
            <SiteInfoForm
              formData={formData}
              verantwoordelijken={verantwoordelijken}
              onChange={handleChange}
            />

            <div className="mt-6 md:mt-8">
              <button
                type="submit"
                className="w-full bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded"
                data-cy="submit-button"
              >
                {t('save')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </AsyncData>
  );
}
