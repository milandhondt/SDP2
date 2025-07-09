import { useTranslation } from 'react-i18next';
import { MdEdit } from 'react-icons/md';

const TableRow = ({ data, columns, onShow, onEdit, onShowGrondplan, cellProps = () => ({}), actionsConfig }) => {
  const { t } = useTranslation();
  return (
    <tr className="border border-gray-300 hover:bg-gray-50" data-cy={`table-row-${data.id}`}>
      {/* Edit Button */}
      {onEdit && (
        <td className="border border-gray-300 px-2 py-2 text-center">
          <button
            className="text-gray-600 hover:text-red-600 transition-colors p-1 rounded-full hover:bg-gray-200"
            onClick={() => onEdit(data.id)}
            title="Edit"
            data-cy="edit-button"
          >
            <MdEdit size={20} />
          </button>
        </td>
      )}

      {/* Data Columns */}
      {columns.map((column, index) => {
        const isClickable = actionsConfig?.[column];
        return (
          <td
            key={index}
            className={`border border-gray-300 px-4 md:py-2 text-center 
              ${isClickable ? 'cursor-pointer hover:underline hover:text-red-700 transition-all' : ''}`}
            data-cy={`table-cell-${column}-${data.id}`}
            {...cellProps(column, data)}
            onClick={isClickable ? () => actionsConfig[column](data) : undefined}
          >
            {data[column]}
          </td>
        );
      })}

      {(onShow || onShowGrondplan) && (
        <td className="border border-gray-300 px-1 py-1 text-center" data-cy={`table-actions-${data.id}`}>
          <div className="inline-flex gap-2">
            {onShow && (
              <div className="border-gray-300 px-1 py-1 text-center">
                <div
                  className="inline-flex gap-2"
                  onClick={() => onShow(data.id)}
                  data-cy={`button-details-${data.id}`}
                >
                  <span className="font-bold hover:cursor-pointer hover:underline hover:text-red-700 transition-all">
                    {t('details')}
                  </span>
                </div>
              </div>
            )}
          </div>
        </td>
      )}
    </tr>
  );
};

export default TableRow;
