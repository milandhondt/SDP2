import TableRow from './TableRow';

const GenericTable = ({ 
  data,
  columns = {},
  onSort,
  onShow,
  onEdit,
  sortConfig = { field: '', direction: 'asc' },
  emptyMessage = 'Er zijn geen gegevens beschikbaar.',
  dataCyPrefix = 'item',
  actionsConfig = {},
}) => {
  if (!data || data.length ===  0) {
    return (
      <div className="flex justify-center items-center h-32" data-cy={`no-${dataCyPrefix}-message`}>
        <h2 className="text-lg font-semibold text-gray-700">{emptyMessage}</h2>
      </div>
    );
  }

  const columnKeys = Object.keys(columns);
  const columnFields = columnKeys.map((key) => columns[key]);

  // Default column config
  const defaultColumnConfig = columnKeys.reduce((config, displayKey) => {
    const dataField = columns[displayKey];
    config[displayKey] = {
      label: displayKey.charAt(0).toUpperCase() + displayKey.slice(1).replace(/_/g, ' '),
      field: dataField,
      render: (item) => item[dataField],
      sortable: true,
    };
    return config;
  }, {});

  // Merge default config with provided config
  const mergedColumnConfig = { ...defaultColumnConfig };

  const renderSortableHeader = (displayKey) => {
    const config = mergedColumnConfig[displayKey];
    const sortField = config.field;
    return (
      <th 
        key={displayKey}
        className={`border border-gray-300 px-4 md:py-2 ${config.sortable ? 'cursor-pointer' : ''} select-none`}
        onClick={() => config.sortable && onSort && onSort(sortField)}
        data-cy={`column-${displayKey}`}
      >
        {config.label}
        {sortConfig?.field === sortField && config.sortable ? 
          (sortConfig.direction === 'asc' ? ' 🔼' : ' 🔽') : 
          ''}
      </th>
    );
  };

  // Transform data for TableRow
  const transformRowData = (item) => {
    const transformedData = { ...item };
    
    // Apply custom renders to the data
    columnKeys.forEach((displayKey) => {
      const config = mergedColumnConfig[displayKey];
      const field = config.field;
      
      // Store the rendered value in the field that TableRow will use
      transformedData[field] = config.render ? config.render(item) : item[field];
    });
    
    return transformedData;
  };

  // Custom cell props for TableRow
  const cellProps = (column, rowData) => ({
    className: 'border border-gray-300 px-4 md:py-2 text-center',
    'data-cy': `${dataCyPrefix}-cell-${column}-${rowData.id}`,
  });

  return (
    <div className="md:overflow-x-auto overflow-x-auto" data-cy={`${dataCyPrefix}-table-container`}>
      <table className="border-separate border-spacing-0 rounded-md border border-gray-300 w-full">
        <thead>
          <tr className="bg-gray-100 text-gray-700 uppercase text-xs sm:text-sm md:text-base font-semibold">
            {onEdit && <th className="border border-gray-300 px-4 md:py-2"></th>}
            {columnKeys.map((displayKey) => renderSortableHeader(displayKey))}
            {onShow && (
              <th className="border border-gray-300 px-4 md:py-2"></th>
            )}
          </tr>
        </thead>
        <tbody data-cy={`${dataCyPrefix}-details`}>
          {data.map((item) => (
            <TableRow
              key={item.id}
              data={transformRowData(item)}
              columns={columnFields}
              onEdit={onEdit}
              onShow={onShow}
              actionsConfig={actionsConfig}
              cellProps={(column, rowData) => cellProps(column, rowData)}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default GenericTable;