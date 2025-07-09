export function convertKPITiles(t, title){
  let convertedTitle = title;
  switch(title.toLocaleLowerCase()){
    case 'algemene gezondheid alle sites': convertedTitle = t('tiles.general-healt-all-sites'); break;
    case 'algemene gezondheid site x': convertedTitle = t('tiles.general-health-site-x'); break;
    case 'productiegraad alle sites gesorteerd (hoog naar laag)': 
      convertedTitle = t('tiles.production-grade-all-sites'); break;
    case 'productiegraad alle sites gesorteerd (laag naar hoog)':
      convertedTitle = t('tiles.production-grade-all-sites.low-to-high'); break;
    case 'top 5 gezonde machines': convertedTitle = t('tiles.top-5-healthy'); break;
    case 'top 5 falende machines': convertedTitle = t('tiles.top-5-failing'); break;
    case 'top 5 machines met nood aan onderhoud': convertedTitle = t('tiles.top-5-needs-maintenance'); break;
    case 'aankomende onderhoudsbeurten': convertedTitle = t('tiles.maintenances-coming'); break;
    case 'laatste 5 onderhoudsbeurten': convertedTitle = t('tiles.last-5-maintenances'); break;
    case 'draaiende machines': convertedTitle = t('tiles.running-machines'); break;
    case 'manueel gestopte machines': convertedTitle = t('tiles.machines-manually-stopped'); break;
    case 'automatisch gestopte machines': convertedTitle = t('tiles.machines-automatically-stopped'); break;
    case 'machines in onderhoud': convertedTitle = t('tiles.machine-in-maintenance'); break;
    case 'startbare machines': convertedTitle = t('tiles.machines-runnable'); break;
    case 'mijn machines': convertedTitle = t('tiles.my-machines'); break;
  }
  return convertedTitle;
}