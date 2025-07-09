import { FaArrowRightLong } from 'react-icons/fa6';
import { FaArrowLeftLong } from 'react-icons/fa6';
import { useTranslation } from 'react-i18next';

export function Pagination({currentPage, setCurrentPage, totalPages, data}){

  const { t } = useTranslation();

  const handlePageChange = (newPage) => {
    if(data && totalPages >= newPage && newPage > 0){
      setCurrentPage(newPage);
    }
  };

  // Om te bepalen of de vorige en volgende knoppen disabled mogen worden
  const isPreviousDisabled = currentPage === 1 || !data;
  const isNextDisabled = !data || currentPage >= totalPages;

  return (
    <div className="flex justify-between items-center w-full my-4 mt-6">
      {/* Vorige button */}
      <button 
        data-cy="previous_page"
        className={`flex items-center select-none gap-2 px-4 py-2 rounded-md transition-all duration-300
          ${isPreviousDisabled 
      ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
      : 'bg-red-500 hover:cursor-pointer text-white hover:shadow-lg hover:bg-red-700'
    }`}
        onClick={() => handlePageChange(currentPage - 1)}
        disabled={isPreviousDisabled}
      >
        <FaArrowLeftLong className="transition-transform"/>
        <span>Vorige</span>
      </button>

      <div className="font-semibold select-none text-center px-4 py-2 rounded-full bg-gray-100">
        {data ? `Pagina ${currentPage} van ${totalPages}` : 'Laden...'}
      </div>
      
      {/* Volgende button */}
      <button 
        data-cy="next_page"
        className={`flex items-center select-none gap-2 px-4 py-2 rounded-md duration-300 transition-all
          ${isNextDisabled 
      ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
      : 'bg-red-500 hover:cursor-pointer text-white hover:shadow-lg hover:bg-red-700'
    }`}
        onClick={() => handlePageChange(currentPage + 1)}
        disabled={isNextDisabled}
      >
        <span>{t('next')}</span>
        <FaArrowRightLong className="transition-transform"/>
      </button>
    </div>
  );
}