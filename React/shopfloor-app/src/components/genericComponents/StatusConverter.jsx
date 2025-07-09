export function convertStatus(status) {
  const statusMap = {
    // Statussen productie machine
    'IN_ONDERHOUD': { text: 'IN ONDERHOUD', color: 'orange' },
    'MANUEEL_GESTOPT': { text: 'MANUEEL GESTOPT', color: 'red' },
    'AUTOMATISCH_GESTOPT': { text: 'AUTOMATISCH GESTOPT', color: 'red' },
    'DRAAIT': { text: 'DRAAIT', color: 'green' },
    'STARTBAAR': { text: 'STARTBAAR', color: 'green'},

    // Statussen machine
    'FALEND': { text: 'FALEND', color: 'red' },
    'GEZOND': { text: 'GEZOND', color: 'green' },
    'NOOD_ONDERHOUD': { text: 'NOOD AAN ONDERHOUD', color: 'orange' },

    // Statussen site
    'ACTIEF': { text: 'ACTIEF', color: 'green' },
    'INACTIEF': { text: 'INACTIEF', color: 'red' },

    // Statussen onderhoud
    'INGEPLAND': { text: 'INGEPLAND', color: 'red' },
    'IN_UITVOERING': { text: 'IN UITVOERING', color: 'orange' },
    'VOLTOOID': { text: 'VOLTOOID', color: 'green' },
  };
  return statusMap[status] || { text: status, color: 'black', bold: false };
}