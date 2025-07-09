import MachineList from '../../components/Sites/siteDetailComponents/MachinesList';
import Information from '../../components/Information';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { FaMapMarkedAlt } from 'react-icons/fa';
import { FaPerson } from 'react-icons/fa6';
import { useNavigate, useParams } from 'react-router-dom';
import useSWR from 'swr';
import { getById } from '../../api/index';
import AsyncData from '../../components/AsyncData';
import { IoMdAddCircleOutline } from 'react-icons/io';
import PageHeader from '../../components/genericComponents/PageHeader';
import GenericButton from '../../components/genericComponents/GenericButton';
import { useAuth } from '../../contexts/auth';
import { useTranslation } from 'react-i18next';

const SiteDetails = () => {
  const { role } = useAuth();
  const { id } = useParams();
  const idAsNumber = Number(id);
  const navigate = useNavigate();

  const handleShowGrondplan = () => {
    navigate(`/sites/${id}/grondplan`);
  };

  const handleOnClickBack = () => {
    navigate('/sites');
  };

  const handleAddMachine = () => {
    navigate('/machines/new');
  };
    
  const {
    data: site = [],
    error: siteError,
    isLoading: siteLoading,
  } = useSWR(id ? `sites/${idAsNumber}` : null, getById);

  const {t} = useTranslation();

  return (
    <>
      <AsyncData error={siteError} loading={siteLoading}>
        <div data-cy="site-details" className="flex justify-between items-center">
          <PageHeader title={`Site | ${site.sitename}`} onBackClick={handleOnClickBack}/>
          <div className="flex gap-4 items-center">
            <GenericButton icon={FaMapMarkedAlt} onClick={handleShowGrondplan} text={t('site.floor-plan')}/>
            {role == 'VERANTWOORDELIJKE' &&
              <GenericButton icon={IoMdAddCircleOutline} onClick={handleAddMachine} text="Machine toevoegen"/>
            }
          </div>
        </div>

        <Information 
          info={t('site.detail.info')}
          icon={IoInformationCircleOutline}
        />

        <Information 
          info={t('site.responsible') + ' ' + site.verantwoordelijke?.lastname
             + ' ' + site.verantwoordelijke?.firstname}
          icon={FaPerson}
        />
      
        {/* Lijst met alle machines*/}
        <MachineList machinesData={site.machines}/>
      </AsyncData>
    </>
  );
};

export default SiteDetails;
