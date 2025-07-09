export function convertStatusI18n(t, status){
  let convertedStatus;
  switch(status){
    case 'MANUEEL_GESTOPT': convertedStatus = t('machine.manueel-gestopt'); break;
    case 'MANUEEL GESTOPT': convertedStatus = t('machine.manueel-gestopt'); break;
    case 'DRAAIT': convertedStatus = t('machine.running'); break;
    case 'STARTBAAR': convertedStatus = t('machine.startable'); break;
    case 'IN_ONDERHOUD': convertedStatus = t('machine.under-maintenance'); break;
    case 'IN ONDERHOUD': convertedStatus = t('machine.under-maintenance'); break;
    case 'AUTOMATISCH_GESTOPT': convertedStatus = t('machine.automatically-stopped'); break;
    case 'AUTOMATISCH GESTOPT': convertedStatus = t('machine.automatically-stopped'); break;

    case 'FALEND': convertedStatus = t('machine.failing'); break;
    case 'GEZOND': convertedStatus = t('machine.healthy'); break;
    case 'NOOD_ONDERHOUD': convertedStatus = t('machine.needs-maintenance'); break;
    case 'NOOD AAN ONDERHOUD': convertedStatus = t('machine.needs-maintenance'); break;

    case 'ACTIEF': convertedStatus = t('status.active'); break;
    case 'INACTIEF': convertedStatus = t('status.inactive'); break;

    case 'INGEPLAND': convertedStatus = t('maintenance.planned'); break;
    case 'IN_UITVOERING': convertedStatus = t('maintenance.in-progress'); break;
    case 'IN UITVOERING': convertedStatus = t('maintenance.in-progress'); break;
    case 'VOLTOOID': convertedStatus = t('maintenance.completed'); break;
  }
  return convertedStatus;
}