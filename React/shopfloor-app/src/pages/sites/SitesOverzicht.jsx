import SiteList from '../../components/Sites/siteOverzichtComponents/SiteList';
import Information from '../../components/Information';
import { IoMdAddCircleOutline } from 'react-icons/io';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { useNavigate } from 'react-router';
import PageHeader from '../../components/genericComponents/PageHeader';
import GenericButton from '../../components/genericComponents/GenericButton';
import AsyncData from '../../components/AsyncData';
import useSWR from 'swr';
import { getAll } from '../../api/index';
import { useAuth } from '../../contexts/auth';
import { useTranslation } from 'react-i18next';

const SitesOverzicht = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  const handleAddSite = () => {
    navigate('/sites/new');
  };

  const {
    data: sites = [],
    isLoading,
    error,
  } = useSWR('sites', getAll);

  const {t} = useTranslation();
  
  return (
    <>
      {/* Pagina titel en knop om een site toe te voegen */}
      <div className="flex justify-between items-center">
        <PageHeader title="Sites" />
        {role === 'MANAGER' && 
            <GenericButton icon={IoMdAddCircleOutline} onClick={handleAddSite} 
              text={t('site.add')} dataCy="add-site-button" />
        }
      </div>

      {/* Informatie over de sites */}
      <Information 
        info={t('site-overview-infobox')}
        icon={IoInformationCircleOutline}
      />
      
      {/* Lijst met alle sites*/}
      <AsyncData loading={isLoading} error={error}>
        <SiteList data={sites} />
      </AsyncData>
    </>
  );
};

export default SitesOverzicht;
