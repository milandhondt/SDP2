import { useTranslation } from 'react-i18next';
import Tile from './Tile';

const TileList = ({ tiles, onDelete, machines, onderhouden }) => {
  const {t} = useTranslation();
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {tiles.length > 0 ? (
        tiles.map((tile) => (
          <Tile key={tile.id} id={tile.id} title={tile.onderwerp}
            machines={machines} onDelete={onDelete} graphType={tile.grafiek}
            onderhouden={onderhouden} />
        ))
      ) : (
        <div className="col-span-full text-center text-gray-500 text-lg font-semibold p-4">
          {t('no-kpi-added')}
        </div>
      )}
    </div>
  );
};

export default TileList;
