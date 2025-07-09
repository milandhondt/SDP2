import { useTranslation } from 'react-i18next';

export default function SiteInfoForm({ formData, verantwoordelijken, onChange }) {
  const {t} = useTranslation();
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6 mb-6 md:mb-8">
      <div>
        <label className="block text-gray-700 mb-2">{t('site.name')}</label>
        <input
          type="text"
          name="sitename"
          value={formData.sitename}
          onChange={onChange}
          className="w-full p-2 border rounded"
          placeholder={t('site.infoform.name.placeholder')}
          required
          data-cy="site-name"
        />
      </div>
  
      <div>
        <label className="block text-gray-700 mb-2">{t('site.responsible')}</label>
        <select
          name="verantwoordelijke_id"
          value={formData.verantwoordelijke_id}
          onChange={onChange}
          className="w-full p-2 border rounded"
          required
          data-cy="verantwoordelijke-select"
        >
          <option 
            value={verantwoordelijken.filter(
              (verantwoordelijke) => verantwoordelijke.id == formData.verantwoordelijke_id).lastname
            }>
            {t('site.select-responsible')}</option>
          {verantwoordelijken.map((verantwoordelijke) => (
            <option key={verantwoordelijke.id} value={verantwoordelijke.id}>
              {verantwoordelijke.lastname || verantwoordelijke.firstname || `User ${verantwoordelijke.id}`}
            </option>
          ))}
        </select>
      </div>
        
      <div>
        <label className="block text-gray-700 mb-2">{t('site.status')}</label>
        <select
          name="status"
          value={formData.status}
          onChange={onChange}
          className="w-full p-2 border rounded"
          data-cy="status-select"
        >
          <option value="ACTIEF">{t('status.active')}</option>
          <option value="INACTIEF">{t('status.inactive')}</option>
        </select>
      </div>
    </div>
  );
}