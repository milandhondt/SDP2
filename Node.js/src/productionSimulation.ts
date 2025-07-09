import type {Productie_Status} from '@prisma/client';
import { Machine_Status} from '@prisma/client';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function simulateProduction() {
  const machines = await prisma.machine.findMany({
    where: { machinestatus: 'DRAAIT' },
  });

  for (const machine of machines) {
    const randValue = Math.random();
    const isGoodProduct = randValue < 0.95;

    if(machine.aantal_goede_producten && machine.aantal_slechte_producten){

      await prisma.machine.update({
        where: { id: machine.id },
        data: {
          aantal_goede_producten: machine.aantal_goede_producten + (isGoodProduct ? 1 : 0),
          aantal_slechte_producten: machine.aantal_slechte_producten + (!isGoodProduct ? 1 : 0),
        },
      });
    }

    await updateMachineStatus(machine.id);
  }
}

// Omdat prisma geen triggers ondersteunt, hebben we een functie hiervoor geschreven:
// Automatisch stoppen van machines (enkel die draaiend zijn) bij 'NOOD_ONDERHOUD'
async function updateMachineStatus(machineId: number) {
  const machine = await prisma.machine.findUnique({ where: { id: machineId } });

  if (!machine) return;

  let totaal_producten = 0;
  let productie_graad = 0;

  if(machine.aantal_goede_producten && machine.aantal_slechte_producten && machine.aantal_goede_producten){
    totaal_producten = machine.aantal_goede_producten + machine.aantal_slechte_producten;
    productie_graad = machine.aantal_goede_producten / (totaal_producten || 1);
  }

  let status = machine.machinestatus;
  let productie_status = machine.productionstatus;

  if (machine.limiet_voor_onderhoud && totaal_producten >= machine.limiet_voor_onderhoud 
    && status === Machine_Status.DRAAIT) {
    productie_status = 'NOOD_ONDERHOUD';
    // Automatisch stoppen van machine:
    status = 'AUTOMATISCH_GESTOPT';
  } else if (productie_graad < 0.5) {
    productie_status = 'FALEND';
  } else {
    productie_status = 'GEZOND';
  }

  await prisma.machine.update({
    where: { id: machineId },
    data: { 
      productionstatus : productie_status as Productie_Status,
      machinestatus: status as Machine_Status,
    },
  });
}

// Start de simulatie elke 10 seconden
export function startProductionSimulation() {
  setInterval(simulateProduction, 10_000);
}
