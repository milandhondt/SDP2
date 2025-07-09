import { useParams } from 'react-router';
import AsyncData from '../../components/AsyncData';
import { getById } from '../../api';
import useSWR from 'swr';
import OnderhoudList from '../../components/onderhouden/OnderhoudOverzichtComponents/OnderhoudList';
import PageHeader from '../../components/genericComponents/PageHeader';
import { useNavigate } from 'react-router-dom';
import Information from '../../components/Information';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { useTranslation } from 'react-i18next';

export default function OnderhoudenMachineOverzicht () {
  const navigate = useNavigate();

  const {t} = useTranslation();

  const {id} = useParams();
  const {
    data: machine,
    isLoading,
    error,
  } = useSWR(`machines/${id}`, getById);

  const handleBackClick = () => {
    navigate('/machines');
  };

  return (
    <>
      <PageHeader title={`${t('maintenance-history')} | ${machine?.code}`} onBackClick={handleBackClick}/>

      <Information 
        info={t('maintenances-machine-overview-infobox')}
        icon={IoInformationCircleOutline}
      />
     
      <AsyncData loading={isLoading} error={error}>
        <OnderhoudList machine={machine}/>
      </AsyncData>
    </>
  );
}