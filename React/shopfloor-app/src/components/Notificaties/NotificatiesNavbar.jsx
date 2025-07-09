import { FiBell } from 'react-icons/fi';
import NotificatieListDropdown from './notificatiesDropdownComponents/NotificatieListDropdown';
import { useState, useEffect, useRef } from 'react';

export default function NotificatieNavbar({ notificaties }) {
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  const handleClickOutside = (event) => {
    if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
      setShowDropdown(false);
    }
  };

  useEffect(() => {
    if (showDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showDropdown]);

  if (!notificaties) {
    return (
      <FiBell className="w-6 h-6 text-black hover:text-gray-600" />
    );
  }

  return (
    <div className='relative inline-block'>
      <div
        className='hover:cursor-pointer transition-all relative'
        onClick={toggleDropdown}
      >
        <FiBell className="w-6 h-6 text-black hover:text-gray-600" />
        <span className="absolute -top-1 -right-1 bg-red-500 
                text-white text-xs font-bold w-5 h-5 flex items-center justify-center rounded-full">
          {notificaties.items.filter((notificatie) => !notificatie.isread).length}
        </span>
      </div>

      {showDropdown && (
        <div ref={dropdownRef} className="absolute right-0 mt-2 z-50">
          <NotificatieListDropdown notificaties={notificaties} toggleDropdown={toggleDropdown} />
        </div>
      )}
    </div>
  );
}
