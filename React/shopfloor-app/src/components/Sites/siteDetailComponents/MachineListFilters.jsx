import { useTranslation } from 'react-i18next';
import { convertStatusI18n } from '../../genericComponents/StatusConverterI18n';

const MachineListFilters = ({
  locatieFilter,
  statusFilter,
  productieStatusFilter,
  techniekerFilter,
  uniqueLocaties = [],
  uniqueStatuses = [],
  uniqueProductieStatuses = [],
  uniqueTechniekers = [],
  onFilterChange,
  onResetFilters,
}) => {
  const {t} = useTranslation();
  return (
    <div className="mb-6 p-4 rounded-md border border-gray-200">
      <div className="flex flex-wrap items-center">
        <h3 className="text-gray-700 font-medium mb-3 w-full">Filters</h3>
        <div className="flex flex-wrap gap-4">
          {/* Locatie filter */}
          <div className="mb-2 md:mb-0">
            <label htmlFor="locatie-filter" className="block text-sm text-gray-600 mb-1">
              {t('machine.location')}
            </label>
            <select
              id="locatie-filter"
              value={locatieFilter}
              onChange={onFilterChange.location}
              className="border border-gray-300 rounded-md px-3 py-2 w-full md:w-auto"
              data-cy="locatie_filter"
            >
              <option value="">{t('machine.all-locations')}</option>
              {uniqueLocaties.map((location) => (
                <option key={location} value={location}>
                  {location}
                </option>
              ))}
            </select>
          </div>
            
          {/* Status filter */}
          <div className="mb-2 md:mb-0">
            <label htmlFor="status-filter" className="block text-sm text-gray-600 mb-1">
              {t('maintenance.status')}
            </label>
            <select
              id="status-filter"
              value={statusFilter}
              onChange={onFilterChange.machinestatus}
              className="border border-gray-300 rounded-md px-3 py-2 w-full md:w-auto"
              data-cy="status_filter"
            >
              <option value="">{t('machine.all-statuses')}</option>
              {uniqueStatuses.map((status) => (
                <option key={status} value={status}>
                  {convertStatusI18n(t, status)}
                </option>
              ))}
            </select>
          </div>
            
          {/* Productie Status filter */}
          <div className="mb-2 md:mb-0">
            <label htmlFor="productie-status-filter" className="block text-sm text-gray-600 mb-1">
              {t('machine.production-status')}
            </label>
            <select
              id="productie-status-filter"
              value={productieStatusFilter}
              onChange={onFilterChange.productionstatus}
              className="border border-gray-300 rounded-md px-3 py-2 w-full md:w-auto"
              data-cy="productie_status_filter"
            >
              <option value="">{t('machine-all-production-statuses')}</option>
              {uniqueProductieStatuses.map((status) => (
                <option key={status} value={status}>
                  {convertStatusI18n(t, status)} 
                </option>
              ))}
            </select>
          </div>
            
          {/* Technieker filter */}
          <div className="mb-2 md:mb-0">
            <label htmlFor="technieker-filter" className="block text-sm text-gray-600 mb-1">
              {t('maintenance.technician')}
            </label>
            <select
              id="technieker-filter"
              value={techniekerFilter}
              onChange={onFilterChange.technieker}
              className="border border-gray-300 rounded-md px-3 py-2 w-full md:w-auto"
              data-cy="technieker_filter"
            >
              <option value="">{t('maintenance.all-technicians')}</option>
              {uniqueTechniekers.map((technieker) => (
                <option key={technieker} value={technieker}>
                  {technieker}
                </option>
              ))}
            </select>
          </div>
            
          {/* Reset filters button */}
          <div className="flex items-end mb-2 md:mb-0">
            <button
              onClick={onResetFilters}
              className="hover:bg-red-700 bg-red-500 text-white py-2 px-4 rounded-md 
                hover:cursor-pointer transition-all duration-300"
              data-cy="reset_filters"
            >
              {t('clear-filters')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
  
export default MachineListFilters;