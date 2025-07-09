import { useTranslation } from 'react-i18next';
import { StatusDisplay } from '../../genericComponents/StatusDisplay';
import {convertStatusI18n} from '../../genericComponents/StatusConverterI18n';

const MachineInfoHeader = ({ machine }) => {
  const {t} = useTranslation();
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
      <div>
        <h2 className="text-2xl md:text-3xl font-semibold mb-2">{t('machine-info')}</h2>
      </div>
      <div className="flex flex-col">
        <h2 data-cy="machine_status" className="text-xl md:text-2xl font-semibold mb-2">
          {t('machine.status')}: <StatusDisplay status={machine.machinestatus} 
            displaystatus={convertStatusI18n(t, machine.machinestatus)} />
        </h2>
      </div>
    </div>
  );
};

export default MachineInfoHeader;