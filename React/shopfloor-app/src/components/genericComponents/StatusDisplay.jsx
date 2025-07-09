import { convertStatus } from './StatusConverter';

export const StatusDisplay = ({ status, displaystatus }) => {
  const statusInfo = convertStatus(status);
  return (
    <span 
      style={{ 
        color: statusInfo.color, 
      }}
    >
      {displaystatus}
    </span>
  );
};