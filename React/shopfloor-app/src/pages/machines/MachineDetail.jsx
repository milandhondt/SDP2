import Information from '../../components/Information';
import { IoInformationCircleOutline } from 'react-icons/io5';
import { useNavigate, useParams } from 'react-router-dom';
import useSWR, {mutate} from 'swr';
import { getById } from '../../api/index';
import AsyncData from '../../components/AsyncData';
import MachineInfoHeader from '../../components/machines/machineDetailsComponents/MachineInfoHeader';
import useSWRMutation from 'swr/mutation';
import { save } from '../../api/index';
import PageHeader from '../../components/genericComponents/PageHeader';
import {StatusDisplay} from '../../components/genericComponents/StatusDisplay';
import { useAuth } from '../../contexts/auth';
import { useTranslation } from 'react-i18next';
import { convertStatusI18n } from '../../components/genericComponents/StatusConverterI18n';

const MachineDetail = () => {
  const { role } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const idAsNumber = Number(id);
  
  const {
    data: machine = [],
    error: machineError,
    isLoading: MachineLoading,
  } = useSWR(id ? `machines/${idAsNumber}` : null, getById);
  
  const {
    trigger: changeMachineStatus,
    isMutating,
  } = useSWRMutation('machines', save, {method: 'PUT'});

  const formatTime = (dateString) => {
    if (!dateString) return '0d 0h 0m';
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now - date) / (1000 * 60));
    const days = Math.floor(diffInMinutes / (24 * 60));
    const hours = Math.floor((diffInMinutes % (24 * 60)) / 60);
    const minutes = diffInMinutes % 60;
    return `${days}d ${hours}h ${minutes}m`;
  };

  const calculateProductiegraad = () => {
    const goedeProducten = machine.aantal_goede_producten || 0;
    const slechteProducten = machine.aantal_slechte_producten || 0;
    const totaalProducten = goedeProducten + slechteProducten;
    if (totaalProducten === 0) return '0.0%';
    return `${((goedeProducten / totaalProducten) * 100).toFixed(2)}%`;
  };

  const handleOnClickBack = () => {
    navigate(-1);
  };

  const toggleMachineStatus = async () => {
    let machinestatus;
    if(machine.machinestatus === 'DRAAIT'){
      machinestatus = 'MANUEEL_GESTOPT';
    } else {
      machinestatus = 'DRAAIT';
    }
    
    await changeMachineStatus(
      {
        id: machine.id,
        code: machine.code, 
        machinestatus: machinestatus,
        productionstatus: machine.productionstatus,
        location: machine.location,
        technician_id: machine.technieker.id, 
        site_id: machine.site.id,
        product_naam: machine.product_naam,
        productinfo: machine.productinfo,
        limiet_voor_onderhoud: machine.limiet_voor_onderhoud,
      },
    );
    // machine status gets refetched so the displayed status is up to date
    mutate(`machines/${idAsNumber}`);
    // notifications also get updated
    mutate('notificaties');
  };
  const {t} = useTranslation();

  const productionData = [
    {
      label: t('machine.production-status'),
      value: <StatusDisplay status={machine.productionstatus}
        displaystatus={convertStatusI18n(t, machine.productionstatus)}
      />,
    },
    {
      label: t('productiongrade'),
      value: calculateProductiegraad(),
    },
    {
      label: t('machine.good-products'),
      value: machine.aantal_goede_producten || 0,
    },
    {
      label: t('machine.bad-products'),
      value: machine.aantal_slechte_producten || 0,
    },
    {
      label: t('maintenance-needed-after'),
      value: `${machine.limiet_voor_onderhoud} producten`,
    },
  ];

  return (
    <>
      <AsyncData error={machineError} loading={MachineLoading}>
        <PageHeader title={`Machine | ${machine.code}`} onBackClick={handleOnClickBack} />

        <Information 
          info={t('machine-detail-infobox') + machine.code}
          icon={IoInformationCircleOutline}
        />

        <div className='grid grid-cols-1 lg:grid-cols-6 gap-4 gap-y-6 mb-12'>
          <div data-cy="machine_details" className="lg:col-span-4 border p-4 rounded-lg mt-4">
            {/* Algemene machine informatie */}
            <MachineInfoHeader machine={machine} />
        
            {/* Machine data grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 gap-y-6">
              {/* Site data grid */}
              <div className="col-span-1 sm:col-span-2 lg:col-span-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="flex flex-col">
                  <span className="text-lg font-medium mb-1">{t('site-name')}</span>
                  <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                    {machine.site?.sitename}
                  </span>
                </div>
                <div className="flex flex-col">
                  <span className="text-lg font-medium mb-1">{t('site-responsible')}</span>
                  <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                    {`${machine.site?.verantwoordelijke?.lastname} ${machine.site?.verantwoordelijke?.firstname}`}
                  </span>
                </div>
                <div className="flex flex-col">
                  <span className="text-lg font-medium mb-1">{t('machine.location')}</span>
                  <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                    {machine.location}
                  </span>
                </div>
                <div className="flex flex-col">
                  <span className="text-lg font-medium mb-1">{t('machine.technician')}</span>
                  <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                    {`${machine.technieker?.lastname} ${machine.technieker?.firstname}`}
                  </span>
                </div>
              </div>

              {/* Uptime/Downtime */}
              <div className="col-span-1 sm:col-span-2 lg:col-span-4">
                <div className="flex flex-col">
                  <span className="text-lg font-medium mb-1">
                    {machine.status === 'DRAAIT' ? 'Uptime' : 'Downtime'}
                  </span>
                  <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                    {formatTime(machine.lastmaintenance)}
                  </span>
                </div>
              </div>

              {/* Onderhoudstabel en Volgend geplande onderhoud */}
              <div className="col-span-1 sm:col-span-2 lg:col-span-4 sm:grid-cols-2 gap-4">
                {/* Onderhoudstabel */}
                <div className="flex flex-col">
                  <span className="text-lg font-medium mb-1">{t('maintenance')}</span>
                  <button 
                    onClick={() => navigate('./onderhouden')}
                    className={`text-lg pl-5 pr-3 py-1 rounded text-left transition-colors ${
                      machine?.onderhouden?.length === 0 
                        ? 'bg-gray-400 text-gray-200 cursor-not-allowed' 
                        : 'bg-red-500 text-white hover:bg-red-600 hover:cursor-pointer'
                    }`}
                    disabled={machine?.onderhouden?.length === 0}
                  >
                    {machine?.onderhouden?.length === 0 ? t('no-maintenance-received') : t('see-maintenance-history')}
                  </button>
                </div>
              </div>

              {/* Product informatie */}
              <div className="flex flex-col col-span-1 sm:col-span-2 lg:col-span-4">
                <span className="text-lg font-medium mb-1">{t('machine-product-name')}</span>
                <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded mb-4">
                  {machine.product_naam}
                </span>
                <span className="text-lg font-medium mb-1">{t('machine-product-information')}</span>
                <span className="text-lg bg-gray-200 pl-5 pr-3 py-2 min-h-19 rounded overflow-y-auto">
                  {machine.productinfo}
                </span>
              </div>
            </div>
          </div>

          {/* Machine Status */}
          <div className="border p-4 rounded-lg mt-4 lg:col-span-2">
            <h2 className="text-2xl md:text-3xl font-semibold mb-6">{t('machine-product-information')}</h2>

            <div className="space-y-4 mb-8">
              {productionData.map((item, index) => {
                if (item.label === 'Productie Status') {
                  return (
                    <div key={index} className="grid grid-cols-2 gap-4">
                      <div className="flex flex-col">
                        <span className="text-lg font-medium mb-1">{item.label}</span>
                        <span data-cy='machine_productie_status' className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                          {item.value}
                        </span>
                      </div>
                      <div className="flex flex-col">
                        <span className="text-lg font-medium mb-1">{t('productiongrade')}</span>
                        <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                          {calculateProductiegraad()}
                        </span>
                      </div>
                    </div>
                  );
                }
                if (item.label === 'Productiegraad') return null;
                return (
                  <div key={index} className="flex flex-col">
                    <span className="text-lg font-medium mb-1">{item.label}</span>
                    <span className="text-lg bg-gray-200 pl-5 pr-3 py-1 rounded">
                      {item.value}
                    </span>
                  </div>
                );
              })}
            </div>
            
            { role !== 'MANAGER' &&
            <div className="mt-8">
              <button
                data-cy='start-stop-button'
                disabled={isMutating}
                onClick={toggleMachineStatus}
                className={`w-full 
                ${machine.machinestatus === 'DRAAIT' ? 
      'bg-red-500 enabled:hover:bg-red-600' : 
      'bg-green-500 enabled:hover:bg-green-600'} 
                font-bold py-3 md:py-4 text-xl md:text-3xl px-4 
                border border-black rounded flex justify-center items-center hover:cursor-pointer`}
              >
                {isMutating 
                  ? t('please-wait')
                  : machine.machinestatus === 'DRAAIT' 
                    ? t('machine-stop').toLocaleUpperCase()
                    : t('machine-start').toLocaleUpperCase()}
              </button>
              <span className="block mt-1 text-sm md:text-base text-center md:text-left font-bold">
                {t('person-responsible-notified')}
              </span>
            </div>
            }
          </div>
        </div>
      </AsyncData>
    </>
  );
};

export default MachineDetail;