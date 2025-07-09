import { useState, useEffect, useMemo } from 'react';
import { StatusDisplay } from '../components/genericComponents/StatusDisplay';
import { Link } from 'react-router-dom';
import { convertStatus } from '../components/genericComponents/StatusConverter';
import { useTranslation } from 'react-i18next';
import { convertStatusI18n } from '../components/genericComponents/StatusConverterI18n';

export default function useMachineData({
  rawData,
  zoekterm,
  locatieFilter,
  statusFilter,
  productieStatusFilter,
  techniekerFilter,
  sortConfig,
  currentPage,
  limit,
}) {

  const [processedMachines, setProcessedMachines] = useState([]);
  const [uniqueLocaties, setUniqueLocaties] = useState([]);
  const [uniqueStatuses, setUniqueStatuses] = useState([]);
  const [uniqueProductieStatuses, setUniqueProductieStatuses] = useState([]);
  const [uniqueTechniekers, setUniqueTechniekers] = useState([]);

  // Process raw data into a more usable format
  useEffect(() => {
    if (rawData && rawData.length > 0) {
      const processed = rawData.map((machine) => ({
        id: machine.id,
        location: machine.location,
        rawStatus: machine.machinestatus,
        rawProductieStatus: machine.productionstatus,
        technieker: `${machine.technieker.lastname} ${machine.technieker.firstname}`,
        aantal_onderhoudsbeurten: machine.onderhouden.length || 0,
      }));
      
      setProcessedMachines(processed);
      
      // Extract unique values for filters
      const locaties = [...new Set(processed.map((machine) => machine.location))].filter(Boolean).sort();
      setUniqueLocaties(locaties);
      
      const statuses = [...new Set(processed.map((machine) => {
        const status = convertStatus(machine.rawStatus);
        return status?.text || '';
      }))].filter(Boolean).sort();
      setUniqueStatuses(statuses);
      
      const productieStatuses = [...new Set(processed.map((machine) => {
        const status = convertStatus(machine.rawProductieStatus);
        return status?.text || '';
      }))].filter(Boolean).sort();
      setUniqueProductieStatuses(productieStatuses);
      
      const techniekers = [...new Set(processed.map((machine) => machine.technieker))].filter(Boolean).sort();
      setUniqueTechniekers(techniekers);
    }
  }, [rawData]);

  // Filter machines based on search term and filters
  const filteredMachines = useMemo(() => {

    return processedMachines.filter((machine) => {
      const statusText = convertStatus(machine.rawStatus)?.text || '';
      const productieStatusText = convertStatus(machine.rawProductieStatus)?.text || '';
  
      const matchesSearch = !zoekterm ||
        machine.location?.toLowerCase().includes(zoekterm.toLowerCase()) ||
        statusText.toLowerCase().includes(zoekterm.toLowerCase()) ||
        productieStatusText.toLowerCase().includes(zoekterm.toLowerCase()) ||
        machine.technieker.toLowerCase().includes(zoekterm.toLowerCase());
  
      const matchesLocatie = !locatieFilter || machine.location === locatieFilter;
      const matchesStatus = !statusFilter || statusText === statusFilter;
      const matchesProductieStatus = !productieStatusFilter || productieStatusText === productieStatusFilter;
      const matchesTechnieker = !techniekerFilter || machine.technieker === techniekerFilter;
  
      return matchesSearch && matchesLocatie && matchesStatus && matchesProductieStatus && matchesTechnieker;
    });
  }, [
    processedMachines,
    zoekterm,
    locatieFilter,
    statusFilter,
    productieStatusFilter,
    techniekerFilter,
  ]);  

  // Sort machines
  const sortedMachines = useMemo(() => {
    return sortMachines(filteredMachines, sortConfig);
  }, [filteredMachines, sortConfig]);
  
  // Paginate machines
  const paginatedMachines = useMemo(() => {
    return paginateMachines(sortedMachines, currentPage, limit);
  }, [sortedMachines, currentPage, limit]);

  const {t} = useTranslation();
  
  // Format machines for display
  const formattedPaginatedMachines = useMemo(() => {
    return paginatedMachines.map((machine) => ({
      id: machine.id,
      location: machine.location,
      machinestatus: <StatusDisplay status={machine.rawStatus} 
        displaystatus={convertStatusI18n(t, machine.rawStatus)} />,
      productionstatus: <StatusDisplay status={machine.rawProductieStatus}
        displaystatus={convertStatusI18n(t, machine.rawProductieStatus)}/>,
      technieker: machine.technieker,
      aantal_onderhoudsbeurten: machine.aantal_onderhoudsbeurten !== 0 ? (
        <Link to={`./${machine.id}/onderhouden`} className='underline'>{machine.aantal_onderhoudsbeurten}</Link>
      ) : machine.aantal_onderhoudsbeurten,
    }));
  }, [paginatedMachines, t]);
  
  return {
    processedMachines,
    filteredMachines,
    sortedMachines,
    paginatedMachines: formattedPaginatedMachines,
    uniqueLocaties,
    uniqueStatuses,
    uniqueProductieStatuses,
    uniqueTechniekers,
  };
}

// Utility functions
function sortMachines(machines, sortConfig) {
  if (!sortConfig.field || !machines) return machines;
  
  const sortedMachines = [...machines]; 
  
  // Map special fields to their sortable values
  const fieldMap = {
    'machinestatus': 'rawStatus',
    'productionstatus': 'rawProductieStatus',
  };
  
  const sortField = fieldMap[sortConfig.field] || sortConfig.field;
  const integerFields = ['id'];
  const sortFn = integerFields.includes(sortField) ? sortInteger : sortString;
  
  return sortedMachines.sort((a, b) => 
    sortFn(a, b, sortField, sortConfig.direction),
  );
}

function paginateMachines(machines, currentPage, limit) {
  if(!machines) return machines;
  const startIndex = (currentPage - 1) * limit;
  return machines.slice(startIndex, startIndex + limit);
}

function sortInteger(a, b, field, direction) {
  return direction === 'asc' 
    ? a[field] - b[field] 
    : b[field] - a[field];
}

function sortString(a, b, field, direction) {
  const valueA = String(a[field]).toLowerCase();
  const valueB = String(b[field]).toLowerCase();
  
  if (valueA < valueB) {
    return direction === 'asc' ? -1 : 1;
  }
  if (valueA > valueB) {
    return direction === 'asc' ? 1 : -1;
  }
  return 0;
}