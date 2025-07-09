import { useTranslation } from 'react-i18next';
import { convertStatusI18n } from '../../genericComponents/StatusConverterI18n';

export default function SiteListFilters({
  statusFilter,
  verantwoordelijkeFilter,
  aantalMachinesMin,
  aantalMachinesMax,
  uniqueStatuses,
  uniqueVerantwoordelijken,
  onFilterChange,
  onResetFilters,
}) {
  const {t} = useTranslation();
  return (
    <div className="mb-6 p-4 rounded-md border border-gray-200">
      <div className="flex flex-wrap items-center">
        <h3 className="text-gray-700 font-medium mb-3 w-full">Filters</h3>
        <div className="flex flex-wrap gap-4">
          {/* Status filter */}
          <div className="mb-2 md:mb-0">
            <label htmlFor="status-filter" className="block text-sm text-gray-600 mb-1">
              {t('site-status')}
            </label>
            <select
              id="status-filter"
              value={statusFilter}
              onChange={onFilterChange.status}
              className="border border-gray-300 rounded-md px-3 py-2 w-full md:w-auto"
              data-cy="status_filter"
            >
              <option value="">{t('site-all-statuses')}</option>
              {uniqueStatuses.map((status) => (
                <option key={status} value={status}>
                  {convertStatusI18n(t, status)}
                </option>
              ))}
            </select>
          </div>
            
          {/* Verantwoordelijke filter */}
          <div className="mb-2 md:mb-0">
            <label htmlFor="verantwoordelijke-filter" className="block text-sm text-gray-600 mb-1">
              {t('site.responsible')}
            </label>
            <select
              id="verantwoordelijke-filter"
              value={verantwoordelijkeFilter}
              onChange={onFilterChange.verantwoordelijke}
              className="border border-gray-300 rounded-md px-3 py-2 w-full md:w-auto"
              data-cy="verantwoordelijke_filter"
            >
              <option value="">{t('sites-all-responsible')}</option>
              {uniqueVerantwoordelijken.map((verantwoordelijke) => (
                <option key={verantwoordelijke} value={verantwoordelijke}>
                  {verantwoordelijke}
                </option>
              ))}
            </select>
          </div>
            
          {/* Aantal machines filter */}
          <div className="mb-2 md:mb-0">
            <label htmlFor="machines-filter-min" className="block text-sm text-gray-600 mb-1">
              {t('sites-machine-amount')}
            </label>
            <div className="flex items-center gap-2">
              <input
                id="machines-filter-min"
                type="text"
                value={aantalMachinesMin}
                onChange={onFilterChange.aantalMachinesMin}
                placeholder="Min"
                className="border border-gray-300 rounded-md px-3 py-2 w-16"
                data-cy="machines_filter_min"
              />
              <span className="text-gray-500">-</span>
              <input
                id="machines-filter-max"
                type="text"
                value={aantalMachinesMax}
                onChange={onFilterChange.aantalMachinesMax}
                placeholder="Max"
                className="border border-gray-300 rounded-md px-3 py-2 w-16"
                data-cy="machines_filter_max"
              />
            </div>
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
}