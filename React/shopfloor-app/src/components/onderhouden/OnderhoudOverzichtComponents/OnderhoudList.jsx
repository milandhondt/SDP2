import { useState } from 'react';
import { Pagination } from '../../genericComponents/Pagination';
import OnderhoudenFilter from './OnderhoudListFilters';
import useOnderhoudData from '../../../hooks/useOnderhoudData';
import GenericTable from '../../genericComponents/GenericTable';
import { pdf } from '@react-pdf/renderer';
import { saveAs } from 'file-saver';
import PdfDocument from '../PdfDocument';
import GenericListHeader from '../../genericComponents/GenericListHeader';
import { useTranslation } from 'react-i18next';

const fetchBase64Logo = async () => {
  try {
    const response = await fetch('/base64DelawareLogo.txt'); // Ensure the file is in the public directory
    const logoData = await response.text();
    return logoData; // Return the base64 string
  } catch (error) {
    console.error('Error loading base64 logo:', error);
    return ''; // Return an empty string or handle it as needed
  }
};

export default function OnderhoudList({machine = []}) {
  const [currentPage, setCurrentPage] = useState(1);
  const [limit, setLimit] = useState(10);
  const [zoekterm, setZoekterm] = useState('');

  // Filter states
  const [statusFilter, setStatusFilter] = useState('');
  const [techniekerFilter, setTechniekerFilter] = useState('');
  
  // Sorting state
  const [sortConfig, setSortConfig] = useState({
    field: 'id',
    direction: 'asc',
  });
  
  // Use the custom hook for filtering and sorting
  const {
    filteredOnderhouden,
    paginatedOnderhouden,
    uniqueStatuses,
    uniqueTechniekers,
  } = useOnderhoudData({
    rawData: machine.onderhouden || [],
    zoekterm,
    statusFilter,
    techniekerFilter,
    sortConfig,
    currentPage,
    limit,
  });

  // Event handlers
  const handleSort = (field) => {
    setSortConfig((prevConfig) => ({
      field,
      direction: 
        prevConfig.field === field
          ? prevConfig.direction === 'asc' ? 'desc' : 'asc'
          : 'asc',
    }));
  };

  const handleSearch = (e) => {
    setZoekterm(e.target.value);
    setCurrentPage(1); // Reset to first page when searching
  };
  
  const handleLimitChange = (e) => {
    setLimit(Number(e.target.value));
    setCurrentPage(1); // Reset to first page when changing limit
  };

  const handleResetFilters = () => {
    setStatusFilter('');
    setTechniekerFilter('');
    setCurrentPage(1); // Reset to first page when clearing filters
  };

  const handleGenerateRapport = async (rowData) => {
    rowData.machine = machine;
    const base64Logo = await fetchBase64Logo();
    const date = new Date(rowData.executiondate).toLocaleDateString();
    const blob = await pdf(<PdfDocument data={rowData} base64Logo={base64Logo} />).toBlob();
    saveAs(blob, `${rowData.id}-${t('maintenance')}-${date}_${t('machine')}-${machine.code}.pdf`);
  };

  // Filter change handlers
  const handleFilterChange = {
    status: (e) => {
      setStatusFilter(e.target.value);
      setCurrentPage(1); // Reset to first page when changing filters
    },
    technieker: (e) => {
      setTechniekerFilter(e.target.value);
      setCurrentPage(1); // Reset to first page when changing filters
    },
  };

  const {t} = useTranslation();

  return (
    <div className="flex-col md:flex-row flex justify-between">
      <div className="w-full">
        <GenericListHeader
          zoekterm={zoekterm}
          onSearch={handleSearch}
          limit={limit}
          onLimitChange={handleLimitChange}
          searchPlaceholder={t('maintenancelist.search.placeholder')}
          listPageSizeSelectorPlaceholder={t('maintenancelist.per-page')}
        />
        
        <OnderhoudenFilter
          statusFilter={statusFilter}
          techniekerFilter={techniekerFilter}
          uniqueStatuses={uniqueStatuses}
          uniqueTechniekers={uniqueTechniekers}
          onFilterChange={handleFilterChange}
          onResetFilters={handleResetFilters}
        />

        <GenericTable
          data={paginatedOnderhouden}
          columns={{
            [t('number')]: 'id',
            [t('maintenance.starttime')]: 'startdate',
            [t('maintenance.endttime')]: 'enddate',
            [t('maintenance.technician.name')]: 'technieker',
            [t('maintenance.reason')]: 'reason',
            [t('maintenance.comments')]: 'comments',
            [t('maintenance.report')]: 'onderhoudsrapport',
            [t('site-status')]: 'status',
          }}
          actionsConfig= {{
            'onderhoudsrapport': handleGenerateRapport,
          }}
          onSort={handleSort}
          sortConfig={sortConfig}
          emptyMessage={t('no-maintenances-for-machine')}
          dataCyPrefix="onderhoud"
        />        

        <Pagination 
          currentPage={currentPage} 
          setCurrentPage={setCurrentPage} 
          data={filteredOnderhouden} 
          totalPages={filteredOnderhouden.length === 0 ? 1 : Math.ceil(filteredOnderhouden.length / limit)} 
        />
      </div>
    </div>
  );
}