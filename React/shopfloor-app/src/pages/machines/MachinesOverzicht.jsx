import useSWR from 'swr';
import AsyncData from '../../components/AsyncData';
import { getAll } from '../../api/index';
import Information from '../../components/Information';
import { IoInformationCircleOutline } from 'react-icons/io5';
import PageHeader from '../../components/genericComponents/PageHeader';
import MachineList from '../../components/machines/machineOverzichtComponents/MachineList';
import { useTranslation } from 'react-i18next';

const MachinesOverzicht = () => {
  const {
    data: machines = [],
    isLoading,
    error,
  } = useSWR('machines', getAll);

  const {t} = useTranslation();

  return (
    <>
      <PageHeader title={t('machines-overview')} />

      <Information
        info={
          t('machine-overview-infobox')
        }
        icon={IoInformationCircleOutline}
      />
      
      <AsyncData loading={isLoading} error={error}>
        <MachineList data={machines.items}/>
      </AsyncData>
    </>
  );
};

export default MachinesOverzicht;