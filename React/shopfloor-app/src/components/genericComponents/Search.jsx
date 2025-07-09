import { IoSearchSharp } from 'react-icons/io5';

const Search = ({ value, onChange, placeholder }) => {
  return (
    <div className="relative w-full md:w-[30%]">
      <IoSearchSharp className="text-gray-500 absolute left-3 top-1/2 transform -translate-y-1/2" />
      <input
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        className="border border-gray-300 rounded-md pl-10 pr-4 py-2 w-full"
        data-cy="search"
      />
    </div>
  );
};

export default Search;
