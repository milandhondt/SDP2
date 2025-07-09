const ListPageCountSelector = ({ title, limit, onLimitChange }) => {
  return (
    <div className="hidden md:flex items-center mt-3 md:mt-0">
      <label htmlFor="page-size" className="mr-2 text-gray-700">
        {title}
      </label>
      <select
        id="page-size"
        value={limit}
        onChange={onLimitChange}
        className="border border-gray-300 rounded-md px-3 py-2"
        data-cy="page_size"
      >
        <option value={5}>5</option>
        <option value={10}>10</option>
        <option value={25}>25</option>
        <option value={50}>50</option>
      </select>
    </div>
  );
};

export default ListPageCountSelector;