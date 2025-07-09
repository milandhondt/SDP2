import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Pagination } from '../../genericComponents/Pagination';
import MachineListFilters from './MachineListFilters';
import useMachineData from '../../../hooks/useSiteMachineData';
import GenericTable from '../../genericComponents/GenericTable';
import GenericListHeader from '../../genericComponents/GenericListHeader';
import { useAuth } from '../../../contexts/auth';
import { useTranslation } from 'react-i18next';

export default function MachineList({machinesData}) {
  const { role, user } = useAuth();
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(1);
  const [limit, setLimit] = useState(10);
  const [zoekterm, setZoekterm] = useState('');
  
  // Filter states
  const [locatieFilter, setLocatieFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [productieStatusFilter, setProductieStatusFilter] = useState('');
  const [techniekerFilter, setTechniekerFilter] = useState('');

  // Sorting state
  const [sortConfig, setSortConfig] = useState({
    field: 'id',
    direction: 'asc',
  });
  
  // Process data with custom hook
  const { 
    filteredMachines,
    paginatedMachines,
    uniqueLocaties,
    uniqueStatuses,
    uniqueProductieStatuses,
    uniqueTechniekers,
  } = useMachineData({
    rawData: machinesData || [],
    zoekterm,
    locatieFilter,
    statusFilter,
    productieStatusFilter, 
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
    setCurrentPage(1);
  };
  
  const handleLimitChange = (e) => {
    setLimit(Number(e.target.value));
    setCurrentPage(1); 
  };

  const handleResetFilters = () => {
    setLocatieFilter('');
    setStatusFilter('');
    setProductieStatusFilter('');
    setTechniekerFilter('');
    setZoekterm('');
    setCurrentPage(1);
  };

  // Navigation handlers
  const handleShow = (id) => {
    navigate(`/machines/${id}`);
  };

  const handleEditMachine = (id) => {
    navigate(`/machines/${id}/edit`);
  };

  // Filter change handlers
  const handleFilterChange = {
    location: (e) => {
      setLocatieFilter(e.target.value);
      setCurrentPage(1);
    },
    machinestatus: (e) => {
      setStatusFilter(e.target.value);
      setCurrentPage(1);
    },
    productionstatus: (e) => {
      setProductieStatusFilter(e.target.value);
      setCurrentPage(1);
    },
    technieker: (e) => {
      setTechniekerFilter(e.target.value);
      setCurrentPage(1);
    },
  };
  
  const filteredPaginatedMachines = user?.role === 'TECHNIEKER' ?
    paginatedMachines?.filter((m) => m.technieker === `${user.lastname} ${user.firstname}`) : paginatedMachines;

  const {t} = useTranslation();

  return (
    <div className="flex-col md:flex-row flex justify-between py-6">
      <div className="w-full">
        <GenericListHeader
          zoekterm={zoekterm}
          onSearch={handleSearch}
          limit={limit}
          onLimitChange={handleLimitChange}
          searchPlaceholder={t('machineslist-searchplaceholder')}
          listPageSizeSelectorPlaceholder={t('machineslist-listpagesizeselectorplaceholder')}
        />

        <MachineListFilters 
          locatieFilter={locatieFilter}
          statusFilter={statusFilter}
          productieStatusFilter={productieStatusFilter}
          techniekerFilter={techniekerFilter}
          uniqueLocaties={uniqueLocaties}
          uniqueStatuses={uniqueStatuses}
          uniqueProductieStatuses={uniqueProductieStatuses}
          uniqueTechniekers={uniqueTechniekers}
          onFilterChange={handleFilterChange}
          onResetFilters={handleResetFilters}
        />

        <GenericTable
          data={filteredPaginatedMachines}
          columns={{
            [t('number')]: 'id',
            [t('machine.location')]: 'location',
            [t('machine.status')]: 'machinestatus',
            [t('machine.production-status')]: 'productionstatus',
            [t('machine.technician')]: 'technieker',
          }}
          onSort={handleSort}
          onShow={handleShow}
          onEdit={role === 'VERANTWOORDELIJKE' ? handleEditMachine : undefined}
          sortConfig={sortConfig}
          emptyMessage={t('no-machines-available')}
          dataCyPrefix="machine"
        />
        
        <Pagination
          currentPage={currentPage}
          setCurrentPage={setCurrentPage}
          data={machinesData}
          totalPages={filteredMachines.length === 0 ? 1 : Math.ceil(filteredMachines.length / limit)}
        />
      </div>
    </div>
  );
}