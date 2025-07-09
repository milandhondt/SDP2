import { useState, useEffect, useMemo } from 'react';
import { StatusDisplay } from '../components/genericComponents/StatusDisplay';
import { convertStatus } from '../components/genericComponents/StatusConverter';
import { convertStatusI18n } from '../components/genericComponents/StatusConverterI18n';
import { useTranslation } from 'react-i18next';

export default function useSiteData({
  rawData,
  zoekterm,
  statusFilter,
  verantwoordelijkeFilter,
  aantalMachinesMin,
  aantalMachinesMax,
  sortConfig,
  currentPage,
  limit,
}) {
  const [processedSites, setProcessedSites] = useState([]);
  const [uniqueStatuses, setUniqueStatuses] = useState([]);
  const [uniqueVerantwoordelijken, setUniqueVerantwoordelijken] = useState([]);
  const [maxMachineCount, setMaxMachineCount] = useState(0);

  useEffect(() => {
    if (rawData && rawData.length > 0) {
      const processed = rawData.map((site) => ({
        id: site.id,
        sitename: site.sitename,
        rawStatus: site.status,
        verantwoordelijke: `${site.verantwoordelijke?.firstname} ${site.verantwoordelijke?.lastname}`,
        aantal_machines: site.machines ? site.machines?.length : 0,
      }));
      
      setProcessedSites(processed);
      
      const statuses = [...new Set(processed.map((site) => {
        const status = convertStatus(site.rawStatus);
        return status?.text || '';
      }))].filter(Boolean).sort();
      setUniqueStatuses(statuses);
      
      const verantwoordelijken = [...new Set(processed.map((site) => site.verantwoordelijke))].filter(Boolean).sort();
      setUniqueVerantwoordelijken(verantwoordelijken);
      
      const maxMachines = Math.max(...processed.map((site) => site.aantal_machines));
      setMaxMachineCount(maxMachines);
    }
  }, [rawData]);

  const filteredSites = useMemo(() => {
    return processedSites.filter((site) => {
      const statusText = convertStatus(site.rawStatus)?.text || '';
      
      // Text search filter
      const matchesSearch = !zoekterm || 
        site.naam?.toLowerCase().includes(zoekterm.toLowerCase()) ||
        statusText.toLowerCase().includes(zoekterm.toLowerCase()) ||
        site.verantwoordelijke?.toLowerCase().includes(zoekterm.toLowerCase());
      
      // Other filters
      const matchesStatus = !statusFilter || statusText === statusFilter;
      const matchesVerantwoordelijke = !verantwoordelijkeFilter || site.verantwoordelijke === verantwoordelijkeFilter;
      
      const minMachines = aantalMachinesMin === '' ? null : parseInt(aantalMachinesMin, 10);
      const matchesMinMachines = minMachines === null || site.aantal_machines >= minMachines;
      
      const maxMachines = aantalMachinesMax === '' ? null : parseInt(aantalMachinesMax, 10);
      const matchesMaxMachines = maxMachines === null || site.aantal_machines <= maxMachines;
      
      return matchesSearch && matchesStatus && matchesVerantwoordelijke && matchesMinMachines && matchesMaxMachines;
    });
  }, [
    processedSites,
    zoekterm,
    statusFilter,
    verantwoordelijkeFilter,
    aantalMachinesMin,
    aantalMachinesMax,
  ]);

  const sortedSites = useMemo(() => {
    return sortSites(filteredSites, sortConfig);
  }, [filteredSites, sortConfig]);
  
  const paginatedSites = useMemo(() => {
    return paginateSites(sortedSites, currentPage, limit);
  }, [sortedSites, currentPage, limit]);

  const {t} = useTranslation();
  
  const formattedPaginatedSites = useMemo(() => {
    return paginatedSites.map((site) => ({
      id: site.id,
      sitename: site.sitename,
      status: <StatusDisplay status={site.rawStatus} 
        displaystatus={convertStatusI18n(t, site.rawStatus)}
      />,
      verantwoordelijke: site.verantwoordelijke,
      aantal_machines: site.aantal_machines,
    }));
  }, [paginatedSites, t]);
  
  return {
    processedSites,
    filteredSites,
    sortedSites,
    paginatedSites: formattedPaginatedSites,
    uniqueStatuses,
    uniqueVerantwoordelijken,
    maxMachineCount,
  };
}

function sortSites(sites, sortConfig) {
  if (!sortConfig.field || !sites) return sites;
  
  const sortedSites = [...sites]; 

  // Map special fields to their sortable values
  const fieldMap = {
    'status': 'rawStatus',
  };

  const sortField = fieldMap[sortConfig.field] || sortConfig.field;
  const integerFields = ['id', 'aantal_machines'];
  const sortFn = integerFields.includes(sortConfig.field) ? sortInteger : sortString;
  
  return sortedSites.sort((a, b) => 
    sortFn(a, b, sortField, sortConfig.direction),
  );
}

function paginateSites(sites, currentPage, limit) {
  if (!sites) return sites;
  return sites.slice((currentPage - 1) * limit, limit * currentPage);
}

function sortInteger(a, b, field, direction) {
  return direction === 'asc' ? a[field] - b[field] : b[field] - a[field];
}

function sortString(a, b, field, direction) {
  const valueA = String(a[field]).toLowerCase();
  const valueB = String(b[field]).toLowerCase();
  
  if (valueA < valueB) return direction === 'asc' ? -1 : 1;
  if (valueA > valueB) return direction === 'asc' ? 1 : -1;
  return 0;
}
