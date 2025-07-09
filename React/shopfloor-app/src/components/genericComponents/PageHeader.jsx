import { FaArrowLeft } from 'react-icons/fa';

export default function PageHeader({ title, onBackClick }) {
  return (
    <div className="flex flex-col md:flex-row md:items-center gap-2 md:gap-4 mb-6 mt-10">
      <div className="flex items-center gap-4">
        {onBackClick && (
          <button 
            className="text-gray-700 hover:text-gray-900 p-2 rounded-full hover:bg-gray-100"
            onClick={onBackClick}
            aria-label="Go back"
          >
            <FaArrowLeft size={24} />
          </button>
        )}
        <h1 className="text-4xl font-semibold"> 
          {title}
        </h1>
      </div>
    </div>
  );
}