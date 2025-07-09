import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import useSWR from 'swr';
import AsyncData from '../../components/AsyncData';
import PageHeader from '../../components/genericComponents/PageHeader';
import SuccessMessage from '../../components/genericComponents/SuccesMessage';
import { getAll, getById, updateMachine, createMachine } from '../../api';
import { useAuth } from '../../contexts/auth';
import { useTranslation } from 'react-i18next';

export default function AddOrEditMachine() {
  const {t} = useTranslation();
  const { role } = useAuth();
  const navigate = useNavigate();
  useEffect(() => {
    if (role !== 'VERANTWOORDELIJKE') {
      navigate('/not-found');
    }
  }, [role, navigate]);

  const { id } = useParams();
  const machineId = id;
  const isNewMachine = !machineId || machineId === 'new';

  const [successMessage, setSuccessMessage] = useState('');
  const [error, setError] = useState(null);
  const [isFormDataInitialized, setIsFormDataInitialized] = useState(false);

  const [formData, setFormData] = useState({
    code: '',
    machinestatus: 'DRAAIT',
    productionstatus: 'GEZOND',
    location: '',
    technician_id: '',
    site_id: '',
    product_naam: '',
    productinfo: '',
    limiet_voor_onderhoud: 100,
  });

  // Fetch machine data if editing an existing machine
  const { data: machineData, error: machineError } = useSWR(
    !isNewMachine ? `/machines/${machineId}` : null,
    () => getById(`/machines/${machineId}`),
  );

  // Fetch techniekers using SWR
  const { data: techniekersData, error: techniekersError } = useSWR('/users', getAll);
  const techniekers = techniekersData?.items.filter((user) => user.rol === 'TECHNIEKER') || [];

  // Fetch sites using SWR
  const { data: sitesData, error: sitesError } = useSWR('/sites', getAll);
  const sites = sitesData?.items || [];

  // Set form data when machine data is loaded - ONLY ONCE
  useEffect(() => {
    if (machineData && !isFormDataInitialized) {
      setFormData({
        code: machineData.code || '',
        location: machineData.locatie || '',
        machinestatus: machineData.status || 'DRAAIT',
        productionstatus: machineData.productie_status || 'GEZOND',
        technician_id: machineData.technieker?.id || '',
        site_id: machineData.site?.id || '',
        product_naam: machineData.product_naam || '',
        productinfo: machineData.product_informatie || '',
        limiet_voor_onderhoud: machineData.limiet_voor_onderhoud || 150,
      });
      setIsFormDataInitialized(true);
    }
  }, [machineData, isFormDataInitialized]);

  // Determine loading and error states
  const isLoading = (!isNewMachine && !machineData && !machineError) ||
    (!techniekersData && !techniekersError) ||
    (!sitesData && !sitesError);
  const fetchError = machineError || techniekersError || sitesError || error;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const machineUpdateData = {
        code: formData.code,
        machinestatus: formData.machinestatus,
        productionstatus: formData.productionstatus,
        location: formData.location,
        limiet_voor_onderhoud: formData.limiet_voor_onderhoud,
        technician_id: formData.technician_id,
        site_id: formData.site_id,
        product_naam: formData.product_naam,
        productinfo: formData.productinfo,
      };

      if (isNewMachine) {
        await createMachine(machineUpdateData);
        setSuccessMessage(t('machine.success.added'));
      } else {
        await updateMachine(machineId, machineUpdateData);
        setSuccessMessage(t('machine.success.updated'));
      }

    } catch (err) {
      console.error(`${isNewMachine ? 'Creation' : 'Update'} failed:`, err);
      setError(`${t('machine.error.1.1')} ${isNewMachine ? 'toevoegen' : 'bijwerken'} ${t('machine.error.1.2')}`);
    }
  };

  const handleOnClickBack = () => {
    navigate(-1);
  };

  const pageTitle = isNewMachine ? t('machine.add-new') : t('machine-edit', { code: formData.code });

  return (
    <AsyncData loading={isLoading} error={fetchError}>
      <div className="p-2 md:p-4">
        <PageHeader title={pageTitle} onBackClick={handleOnClickBack} />

        {successMessage && <SuccessMessage message={successMessage} />}

        <div className="bg-white p-3 md:p-6 border rounded">
          <h2 className="text-2xl md:text-3xl font-semibold mb-4 md:mb-6">{t('machine-info')}</h2>

          <form onSubmit={handleSubmit} data-cy="machine-form">
            <div className="mb-4">
              <label htmlFor="code" className="block text-sm font-medium text-gray-700">{t('machine-code')}</label>
              <input
                type="text"
                name="code"
                value={formData.code}
                onChange={handleChange}
                className="mt-1 p-2 border rounded w-full"
                required
              />
            </div>

            <div className="mb-4">
              <label htmlFor="locatie" className="block text-sm font-medium text-gray-700">
                {t('machine.location')}
              </label>
              <input
                type="text"
                name="locatie"
                value={formData.location}
                onChange={handleChange}
                className="mt-1 p-2 border rounded w-full"
                required
              />
            </div>

            <div className="mb-4">
              <label htmlFor="site_id" className="block text-sm font-medium text-gray-700">{t('site')}</label>
              <select
                name="site_id"
                value={formData.site_id}
                onChange={handleChange}
                className="mt-1 p-2 border rounded w-full"
                required
              >
                <option value="">{t('select.site-no-semicolon')}</option>
                {sites.map((site) => (
                  <option key={site.id} value={site.id}>
                    {site.naam}
                  </option>
                ))}
              </select>
            </div>

            <div className="mb-4">
              <label htmlFor="technieker_id" className="block text-sm font-medium text-gray-700">
                {t('machine.technician')}
              </label>
              <select
                name="technieker_id"
                value={formData.technician_id}
                onChange={handleChange}
                className="mt-1 p-2 border rounded w-full"
                required
              >
                <option value="">{t('machine-select-technician')}</option>
                {techniekers.map((tech) => (
                  <option key={tech.id} value={tech.id}>
                    {tech.lastname} {tech.firstname}
                  </option>
                ))}
              </select>
            </div>

            <div className="mb-4">
              <label htmlFor="product_naam" className="block text-sm font-medium text-gray-700">
                {t('machine-product-name')}
              </label>
              <input
                type="text"
                name="product_naam"
                value={formData.product_naam}
                onChange={handleChange}
                className="mt-1 p-2 border rounded w-full"
                required
              />
            </div>

            <div className="mb-4">
              <label htmlFor="product_informatie" className="block text-sm font-medium text-gray-700">
                {t('machine-product-info')}
              </label>
              <textarea
                name="product_informatie"
                value={formData.productinfo}
                onChange={handleChange}
                className="mt-1 p-2 border rounded w-full"
                rows="3"
              />
            </div>

            <div className="mb-4">
              <label htmlFor="limiet_voor_onderhoud" className="block text-sm font-medium text-gray-700">
                {t('machine-amount-products-before-maintenance')}
              </label>
              <input
                type="number"
                name="limiet_voor_onderhoud"
                value={formData.limiet_voor_onderhoud}
                onChange={handleChange}
                className="mt-1 p-2 border rounded w-full"
                required
              />
            </div>

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