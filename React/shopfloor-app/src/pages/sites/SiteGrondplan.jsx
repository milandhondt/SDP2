import useSWR from 'swr';
import { getById } from '../../api';
import AsyncData from '../../components/AsyncData';
import { useParams } from 'react-router-dom';
import Grondplan from '../../components/machines/grondplanMachinesComponents/Grondplan';
import PageHeader from '../../components/genericComponents/PageHeader';
import { useNavigate } from 'react-router-dom';

const SiteGrondPlan = () => {
  const { id } = useParams();
  const idAsNumber = Number(id);
  const navigate = useNavigate();
    
  const {
    data: site = [],
    error: siteError,
    isLoading: siteLoading,
  } = useSWR(id ? `sites/${idAsNumber}` : null, getById);

  const machines = site.machines || [];

  const handleOnClickBack = () => {
    navigate(`/sites/${site.id}`);
  };

  return (
    <div>
      <PageHeader title={site.naam} onBackClick={handleOnClickBack} />
      <div data-cy="site-grondplan-container">
        <div className="w-full" data-cy="site-grondplan-content">
          <AsyncData error={siteError} loading={siteLoading} data-cy="async-data">
            <Grondplan machines={machines} data-cy="grondplan-component" />
          </AsyncData>
        </div>
      </div>
    </div>
  );
};

export default SiteGrondPlan;
