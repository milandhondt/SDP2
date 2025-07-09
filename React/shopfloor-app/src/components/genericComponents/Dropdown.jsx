import { useState } from 'react';
import { convertKPITiles } from '../kpiTiles/ConvertKPITileTitle';
import { useTranslation } from 'react-i18next';

export default function Dropdown({ label, options, onSelect }) {
  const [isOpen, setIsOpen] = useState(false);
  const {t} = useTranslation();

  return (
    <div className="relative inline-block text-left ml-4">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="px-4 py-2 bg-[rgb(171,155,203)] hover:bg-[rgb(151,135,183)] 
        text-white rounded-lg hover:cursor-pointer focus:outline-none"
      >
        {label}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-48 bg-white bg-opacity-100 border
         border-gray-200 rounded-lg shadow-lg z-10">
          <ul className="py-2">
            {options.map((option) => (
              <li key={option.id}>
                <button
                  className="w-full text-left block px-4 py-2 hover:bg-gray-200 focus:bg-gray-300 focus:outline-none"
                  onClick={() => {
                    onSelect(option.id);
                    setIsOpen(false);
                  }}
                >
                  {convertKPITiles(t, option.onderwerp)}
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
