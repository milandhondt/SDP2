import { useEffect, useMemo } from 'react';
import { StatusDisplay } from '../components/genericComponents/StatusDisplay';
import { convertStatus } from '../components/genericComponents/StatusConverter';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { convertStatusI18n } from '../components/genericComponents/StatusConverterI18n';

export default function useOnderhoudData({
  rawData,
  zoekterm,
  statusFilter,
  techniekerFilter,
  sortConfig,
  currentPage,
  limit,
}) {
  const [processedOnderhouden, setProcessedOnderhouden] = useState([]);
  const [uniqueStatuses, setUniqueStatuses] = useState([]);
  const [uniqueTechniekers, setUniqueTechniekers] = useState([]);
  
  useEffect(() => {
    if(rawData && rawData.length > 0) {
      const processed = rawData.map((onderhoud) => ({
        id: onderhoud.id,
        technieker: `${onderhoud.technieker.lastname} ${onderhoud.technieker.firstname}`,
        executiondate: onderhoud.executiondate,
        startdate: onderhoud.startdate,
        enddate: onderhoud.enddate,
        reason: onderhoud.reason,
        rawStatus: onderhoud.status,
        comments: onderhoud.comments,
        onderhoudsrapport: '',
      }));
      
      setProcessedOnderhouden(processed);

      const statuses = [...new Set(processed.map((onderhoud) => {
        const status = convertStatus(onderhoud.rawStatus);
        return status?.text || '';
      }))].filter(Boolean).sort();
      setUniqueStatuses(statuses);

      const techniekers = [...new Set(processed.map((onderhoud) => onderhoud.technieker))].filter(Boolean).sort();
      setUniqueTechniekers(techniekers);

    }
  }, [rawData]);

  // Apply filters
  const filteredOnderhouden = useMemo(() => {
    return processedOnderhouden.filter((onderhoud) => {
      const statusText = convertStatus(onderhoud.rawStatus)?.text || '';

      const matchesSearch = !zoekterm ||
        onderhoud.technieker?.toLowerCase().includes(zoekterm.toLowerCase()) ||
        onderhoud.executiondate?.toLowerCase().includes(zoekterm.toLowerCase()) ||
        onderhoud.reason?.toLowerCase().includes(zoekterm.toLowerCase()) ||
        statusText.toLowerCase().includes(zoekterm.toLowerCase()) ||
        onderhoud.comments?.toLowerCase().includes(zoekterm.toLowerCase());

      const matchesStatus = !statusFilter || statusText === statusFilter;
      const matchesTechnieker = !techniekerFilter || onderhoud.technieker === techniekerFilter;

      return matchesSearch && matchesStatus && matchesTechnieker;
    });
  }, [processedOnderhouden, zoekterm, statusFilter, techniekerFilter]);

  // Sorting
  const sortedOnderhouden = useMemo(() => {
    return sortOnderhouden(filteredOnderhouden, sortConfig);
  }, [filteredOnderhouden, sortConfig]);

  // Pagination
  const paginatedOnderhouden = useMemo(() => {
    return paginateOnderhouden(sortedOnderhouden, currentPage, limit);
  }, [sortedOnderhouden, currentPage, limit]);

  const {t} = useTranslation();

  // Format final data with UI components
  const formattedPaginatedOnderhouden = useMemo(() => {
    return paginatedOnderhouden.map((onderhoud) => ({
      ...onderhoud,
      status: <StatusDisplay status={onderhoud.rawStatus}
        displaystatus={convertStatusI18n(t, onderhoud.rawStatus)}
      />,
      onderhoudsrapport: 
        <span className='bg-red-500 p-2 rounded text-white 
        hover:bg-red-700 hover:cursor-pointer'>{t('generate-report')}</span>,
    }));
  }, [paginatedOnderhouden, t]);

  return {
    processedOnderhouden,
    filteredOnderhouden,
    sortedOnderhouden,
    paginatedOnderhouden: formattedPaginatedOnderhouden,
    uniqueStatuses,
    uniqueTechniekers,
  };
}

function sortOnderhouden(onderhouden, sortConfig) {
  if (!sortConfig?.field || !onderhouden) return onderhouden;

  const sortedOnderhouden = [...onderhouden];

  // Map special fields to their sortable values
  const fieldMap = {
    status: 'rawStatus',
  };

  const sortField = fieldMap[sortConfig.field] || sortConfig.field;
  const integerFields = ['id'];
  const sortFn = integerFields.includes(sortConfig.field) ? sortInteger : sortString;

  return sortedOnderhouden.sort((a, b) => sortFn(a, b, sortField, sortConfig.direction));
}

function paginateOnderhouden(onderhouden, currentPage, limit) {
  if (!onderhouden) return onderhouden;
  return onderhouden.slice((currentPage - 1) * limit, limit * currentPage);
}

function sortInteger(a, b, field, direction) {
  return direction === 'asc' ? a[field] - b[field] : b[field] - a[field];
}

function sortString(a, b, field, direction) {
  const valueA = String(a[field] || '').toLowerCase();
  const valueB = String(b[field] || '').toLowerCase();

  if (valueA < valueB) return direction === 'asc' ? -1 : 1;
  if (valueA > valueB) return direction === 'asc' ? 1 : -1;
  return 0;
}
