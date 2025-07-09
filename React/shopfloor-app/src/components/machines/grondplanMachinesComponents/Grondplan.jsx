import { useState, useEffect } from 'react';
import { Stage, Layer, Group } from 'react-konva';
import { MdFactory } from 'react-icons/md';
import { useTranslation } from 'react-i18next';
import { convertStatusI18n } from '../../genericComponents/StatusConverterI18n';

const factoryZones = [
  { xMin: 320, xMax: 750, yMin: 130, yMax: 370 }, 
  { xMin: 550, xMax: 880, yMin: 280, yMax: 470 },  
];

const isInsideFactory = (x, y) => {
  return factoryZones.some((zone) => 
    x >= zone.xMin && x <= zone.xMax && y >= zone.yMin && y <= zone.yMax,
  );
};

const isOverlapping = (machine, machines) => {
  return machines.some((otherMachine) => {
    const dx = machine.x - otherMachine.x;
    const dy = machine.y - otherMachine.y;
    const distance = Math.sqrt(dx * dx + dy * dy);
    return distance < 120; 
  });
};

const Grondplan = ({ machines }) => {
  const {t} = useTranslation();
  const [randomizedMachines, setRandomizedMachines] = useState([]);
  const [selectedMachine, setSelectedMachine] = useState(null);

  useEffect(() => {
    if (machines && machines.length > 0) {
      const placedMachines = [];

      machines.forEach((machine) => {
        let newMachine = { ...machine };
        let validPosition = false;
        let attempts = 0;

        while (!validPosition && attempts < 1000) {
          const zone = factoryZones[Math.floor(Math.random() * factoryZones.length)];
          const x = Math.random() * (zone.xMax - zone.xMin - 120) + zone.xMin + 60;
          const y = Math.random() * (zone.yMax - zone.yMin - 120) + zone.yMin + 60;

          if (isInsideFactory(x, y) && !isOverlapping({ x, y }, placedMachines)) {
            newMachine.x = x;
            newMachine.y = y;
            validPosition = true;
          }
          attempts++;
        }

        if (validPosition) {
          placedMachines.push(newMachine);
        }
      });

      setRandomizedMachines(placedMachines);
    }
  }, [machines]);

  const getIconColor = (status) => (status === 'DRAAIT' ? 'green' : 'red');

  return (
    <div className="text-center relative bg-white mb-10" data-cy="grondplan">
      <div className="flex justify-center border py-2 border-black rounded-lg relative overflow-x-scroll bg-gray-400"
        data-cy="map">
        <div className="absolute top-[250px] left-[500px] w-[400px] h-[250px] bg-gray-700 
        border-4 border-gray-600 z-10 rounded-lg"></div>
        <div className="absolute top-10 left-1/4 w-[600px] h-[300px] bg-gray-700 border-4 
        border-gray-600 z-20 rounded-lg"></div>

        <Stage width={800} height={500} className="rounded-lg">
          <Layer>
            {randomizedMachines.map((machine) => (
              <Group key={machine.id} x={machine.x} y={machine.y} data-cy="machine-marker"></Group>
            ))}
          </Layer>
        </Stage>

        <div className="absolute top-0 left-0 w-[800px] h-[500px] z-30">
          {randomizedMachines.map((machine) => (
            <div
              key={machine.id}
              className="absolute cursor-pointer text-center"
              style={{
                top: machine.y - 35, 
                left: machine.x - 35, 
              }}
              onClick={() => setSelectedMachine(machine)}
              data-cy="machine-marker"
            >
              <MdFactory className="rounded-lg" size={70} color={getIconColor(machine.machinestatus)} />
              <div className="text-xs mt-1 w-[70px] text-center 
              font-semibold  p-1 rounded-md text-white">{machine.location}</div> 
            </div>
          ))}
        </div>
      </div>

      {selectedMachine && (
        <div className="mt-4 p-6 border border-gray-400 rounded-lg shadow-lg bg-gray-100" data-cy="machine-details">
          <h3 className="text-lg font-bold mb-2">{selectedMachine.name}</h3>
          <p><strong>{t('machine.id')}:</strong> {selectedMachine.id}</p>
          <p><strong>{t('machine.location')}:</strong> {selectedMachine.location}</p>
          <p><strong>{t('machine.status')}:</strong> {convertStatusI18n(t, selectedMachine.machinestatus)}</p>
          <p><strong>{t('machine.production-status')}: </strong>
            {convertStatusI18n(t, selectedMachine.productionstatus)}
          </p>
          <button 
            className="mt-4 bg-red-600 text-white px-4 py-2 rounded-lg shadow-md hover:bg-red-700 transition"
            onClick={() => setSelectedMachine(null)}
            data-cy="close-machine-details"
          >
            {t('close')}
          </button>
        </div>
      )}
    </div>
  );
};

export default Grondplan;
