import { prisma } from '../data';
import ServiceError from '../core/serviceError';
import handleDBError from './_handleDBError';
import { getKPIid } from './kpi';
import { Machine_Status, Productie_Status } from '@prisma/client';
import type { Machine, MachineCreateInput } from '../types/machine';
import { getLogger } from '../core/logging';

// Gegevens voor een machine die we willen ophalen
const SELECT_MACHINE = {
  id: true,
  code: true,
  machinestatus: true,
  lastmaintenance: true,
  aantal_goede_producten: true,
  aantal_slechte_producten: true,
  limiet_voor_onderhoud: true,
  location: true,
  productionstatus: true,
  technieker: {
    select: {
      id: true,
      firstname: true,
      lastname: true,
    },
  },
  site: {
    select: {
      id: true,
      sitename: true,
      verantwoordelijke: true,
    },
  },
  product_naam: true,
  productinfo: true,
  onderhouden: {
    select: {
      id: true,
      machine_id: true,
      executiondate: true,
      startdate: true,
      enddate: true,
      reason: true,
      status: true,
      comments: true,
      technieker: {
        select: {
          id: true,
          firstname: true,
          lastname: true,
        },
      },
    },
  },
};

export const getAllMachines = async (user_id: number, user_roles: string[]): Promise<Machine[]> => {
  try {
    let machines: Machine[] = [];

    if (user_roles.includes('MANAGER')) {
      // Manager mag alle sites zien:
      machines = await prisma.machine.findMany({
        select: SELECT_MACHINE,
      });
    } else if (user_roles.includes('TECHNIEKER')) {
      // Technieker mag enkel zijn eigen machines zien:
      machines = await prisma.machine.findMany({
        where: {
          OR: [
            {
              technician_id: user_id,
            },
            {
              onderhouden: {
                some: {
                  technician_id: user_id,
                },
              },
            },
          ],
        },
        select: SELECT_MACHINE,
      });
    } else if (user_roles.includes('VERANTWOORDELIJKE')) {
      // Verantwoordelijke mag enkel machines van zijn sites zien:
      machines = await prisma.machine.findMany({
        where: {
          site: {
            verantwoordelijke_id: user_id,
          },
        },
        select: SELECT_MACHINE,
      });
    }

    return machines;
  } catch (error) {
    if (error instanceof ServiceError) {
      throw error;
    }
    throw handleDBError(error);
  }
};

export const createMachine = async (data: MachineCreateInput): Promise<Machine> => {
  try {
    getLogger().info(`Creating machine with code ${data.code}`);
    // Check if the technieker/user exists 
    const technieker = await prisma.gebruiker.findUnique({
      where: { id: data.technician_id },
      select: { role: true },
    });

    if (!technieker) {
      throw new Error('Technieker does not exist.');
    }

    // if user/technieker is not a valid TECHNIEKER:
    if (technieker.role !== 'TECHNIEKER') {
      throw new Error('The gebruiker is not a valid TECHNIEKER.');
    }

    // Create the machine
    const machine = await prisma.machine.create({
      data: {
        code: data.code,
        location: data.location,
        machinestatus: Machine_Status.DRAAIT,
        lastmaintenance: new Date(),
        productionstatus: Productie_Status.GEZOND,
        aantal_goede_producten: 0,
        aantal_slechte_producten: 0,
        limiet_voor_onderhoud: data.limiet_voor_onderhoud,
        technieker: {
          connect: { id: data.technician_id },
        },
        site: {
          connect: { id: data.site_id },
        },
        product_naam: data.product_naam,
        productinfo: data.productinfo,
      },
      select: SELECT_MACHINE,
    });

    return machine;
  } catch (error) {
    throw handleDBError(error);
  }
};

export const getMachineById = async (user_id: number, user_roles: string[], id: number) => {
  try {
    let machine: Machine | null = null;
    if (user_roles.includes('MANAGER')) {
      machine = await prisma.machine.findUnique({
        where: { id },
        select: SELECT_MACHINE,
      });
    } else if (user_roles.includes('TECHNIEKER')) {
      machine = await prisma.machine.findFirst({
        where: {
          id,
          OR: [
            { technician_id: user_id },
            {
              onderhouden: {
                some: {
                  technician_id: user_id,
                },
              },
            },
          ],
        },
        select: SELECT_MACHINE,
      });
    } else if (user_roles.includes('VERANTWOORDELIJKE')) {
      machine = await prisma.machine.findUnique({
        where: {
          id,
          site: {
            verantwoordelijke_id: user_id,
          },
        },
        select: SELECT_MACHINE,
      });
    }

    if (!machine) {
      throw ServiceError.forbidden('Machine niet gevonden');
    }

    return machine;
  } catch (error) {
    throw handleDBError(error);
  }
};

export const updateMachineById =
  async (user_roles: string[], id: number, changes: any) => {
    updateMachineKPIs();
    try {
      const previousMachine = await prisma.machine.findUnique({
        where: { id },
        select: {
          machinestatus: true,
        },
      });

      if (!previousMachine) {
        throw ServiceError.notFound('Machine niet gevonden');
      }

      const {
        code,
        location,
        technician_id,
        site_id,
        product_naam,
        productinfo,
        limiet_voor_onderhoud,
        machinestatus,
        productionstatus,
      } = changes;

      // Prepare update data with only defined fields
      const updateData: any = {};

      if (code !== undefined) updateData.code = code;
      if (location !== undefined) updateData.location = location;
      if (limiet_voor_onderhoud !== undefined) updateData.limiet_voor_onderhoud = limiet_voor_onderhoud;
      if (machinestatus !== undefined) updateData.machinestatus = machinestatus as Machine_Status;
      if (productionstatus !== undefined) updateData.productionstatus = productionstatus;
      if (product_naam !== undefined) updateData.product_naam = product_naam;
      if (productinfo !== undefined) updateData.productinfo = productinfo;

      if (technician_id !== undefined) {
        updateData.technieker = {
          connect: { id: technician_id },
        };
      }

      if (site_id !== undefined) {
        updateData.site = {
          connect: { id: site_id },
        };
      }

      // if status is changed:
      if (machinestatus !== undefined && previousMachine.machinestatus !== machinestatus) {
        if (user_roles.includes('VERANTWOORDELIJKE') || user_roles.includes('TECHNIEKER')) {
          updateData.lastmaintenance = new Date();
        } else {
          throw ServiceError.forbidden('Deze actie is niet toegestaan!');
        }
      }

      const machine = await prisma.machine.update({
        where: { id },
        data: updateData, // Use the prepared updateData object here
        select: SELECT_MACHINE,
      });

      // Create notification if status changed
      if (machinestatus !== undefined && previousMachine.machinestatus !== machinestatus) {
        await prisma.notificatie.create({
          data: {
            message: `Machine ${machine.id} ${machine.machinestatus}`,
          },
        });
      }

      return machine;
    } catch (error) {
      throw handleDBError(error);
    }
  };

async function safeCreateKPIWaarden(data: any) {
  let retries = 3;
  while (retries > 0) {
    try {
      await prisma.kPIWaarde.createMany({
        data,
        skipDuplicates: true,
      });
      return;
    } catch (error: any) {
      if (error.code === 'P2034' || error.message.includes('write conflict')) {
        retries--;
        console.warn(`Retrying transaction... (${3 - retries}/3)`);
        await new Promise((res) => setTimeout(res, 100));
      } else {
        throw error;
      }
    }
  }
  throw new Error('Transaction failed after 3 retries');
}

export const updateMachineKPIs = async () => {
  try {

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    await prisma.kPIWaarde.deleteMany({});

    // Productiegraad per site
    const productieDataPerSite = await prisma.machine.groupBy({
      by: ['site_id'],
      _sum: {
        aantal_goede_producten: true,
        aantal_slechte_producten: true,
      },
    });

    const kpiDataProductiegraad = productieDataPerSite.map(({ site_id, _sum }) => {
      const goedeProducten = _sum.aantal_goede_producten || 0;
      const slechteProducten = _sum.aantal_slechte_producten || 0;
      const totaalProducten = goedeProducten + slechteProducten;

      const productiegraad = totaalProducten === 0 ? 0 : goedeProducten / totaalProducten;

      return {
        site_id,
        productiegraad,
      };
    });

    const kpiDataHoogLaag = [...kpiDataProductiegraad]
      .sort((a, b) => b.productiegraad - a.productiegraad)
      .map(({ site_id, productiegraad }) => ({
        kpi_id: getKPIid('PRODUCTIEGRAADHOOGLAAG'),
        datum: today,
        waarde: productiegraad.toFixed(2),
        site_id: String(site_id),
      }));

    const kpiDataLaagHoog = [...kpiDataProductiegraad]
      .sort((a, b) => a.productiegraad - b.productiegraad)
      .map(({ site_id, productiegraad }) => ({
        kpi_id: getKPIid('PRODUCTIEGRAADLAAGHOOG'),
        datum: today,
        waarde: productiegraad.toFixed(2),
        site_id: String(site_id),
      }));

    await safeCreateKPIWaarden([...kpiDataHoogLaag, ...kpiDataLaagHoog]);

    //Algemene gezondheid per site
    const machinesPerSite = await prisma.machine.groupBy({
      by: ['site_id', 'productionstatus'],
      _count: { id: true },
    });

    const siteHealthData: Record<number, { gezond: number; falend: number }> = {};

    machinesPerSite.forEach(({ site_id, productionstatus, _count }) => {
      if (!siteHealthData[site_id]) {
        siteHealthData[site_id] = { gezond: 0, falend: 0 };
      }

      if (productionstatus === 'GEZOND') {
        siteHealthData[site_id].gezond += _count.id;
      } else if (productionstatus === 'FALEND' || productionstatus === 'NOOD_ONDERHOUD') {
        siteHealthData[site_id].falend += _count.id;
      }
    });

    const kpiDataPerSite = Object.entries(siteHealthData).map(([site_id, { gezond, falend }]) => ({
      kpi_id: getKPIid('SITE_GEZONDHEID'),
      datum: today,
      waarde: ((gezond / (gezond + falend)) * 100).toFixed(2),
      site_id: site_id,
    }));

    await safeCreateKPIWaarden(kpiDataPerSite);

    // Algemene gezondheid alle sites
    const totaalGezond = kpiDataPerSite.reduce((sum, { waarde }) => sum + parseFloat(String(waarde)), 0);
    const totaalSites = kpiDataPerSite.length;
    const algemeneGezondheid = totaalSites === 0 ? '0' : (totaalGezond / totaalSites).toFixed(2);

    const KPI_data_algemeneGezondheid = {
      kpi_id: getKPIid('ALGEMENE_GEZONDHEID'),
      datum: today,
      waarde: algemeneGezondheid,
      site_id: null,
    };

    await safeCreateKPIWaarden(KPI_data_algemeneGezondheid);

    // Machines per status
    const machinesPerStatus = await prisma.machine.groupBy({
      by: ['machinestatus'],
      _count: { id: true },
    });

    const KPI_data_machinesPerStatus = machinesPerStatus.map((statusgroep) => ({
      kpi_id: getKPIid(statusgroep.machinestatus),
      datum: today,
      waarde: statusgroep._count.id.toString(),
      site_id: null,
    }));

    await safeCreateKPIWaarden(KPI_data_machinesPerStatus);

    // Aankomende onderhoudsbeurten
    const aankomendeOnderhoudsbeurten = await prisma.onderhoud.findMany({
      where: {
        executiondate: {
          gt: today,
        },
      },
    });

    const onderhoudIds = aankomendeOnderhoudsbeurten.map((onderhoud) => onderhoud.id);

    const aankomendeOnderhoudsbeurtenKPIData = [{
      kpi_id: getKPIid('AANKOMEND_ONDERHOUD'),
      datum: today,
      waarde: onderhoudIds.join(','),
    }];
    await safeCreateKPIWaarden(aankomendeOnderhoudsbeurtenKPIData);

    // Laatste 5 onderhoudsbeurten
    const laatsteOnderhouden = await prisma.onderhoud.findMany({
      select: {
        id: true,
      },
      orderBy: {
        executiondate: 'desc',
      },
      take: 5,
    });

    const laatsteOnderhoudIds = laatsteOnderhouden.map((onderhoud) => onderhoud.id);

    const laatste5OnderhoudenKPIData = [{
      kpi_id: getKPIid('LAATSTEONDERHOUDEN'),
      datum: today,
      waarde: laatsteOnderhoudIds.join(','),
    }];

    await safeCreateKPIWaarden(laatste5OnderhoudenKPIData);

    //Machines per productiestatus
    const machinesPerProductieStatus = await prisma.machine.findMany({
      select: {
        productionstatus: true,
        id: true,
      },
      orderBy: {
        machinestatus: 'asc',
      },
    });

    const machinesPerProductieStatusGrouped = machinesPerProductieStatus.reduce((acc, machine) => {
      if (!acc[machine.productionstatus]) {
        acc[machine.productionstatus] = [];
      }
      acc[machine.productionstatus]?.push(machine.id);
      return acc;
    }, {} as Record<string, number[]>);

    const KPI_data_machinesPerProductieStatus = Object.entries(machinesPerProductieStatusGrouped).map(
      ([productieStatus, ids]) => ({
        kpi_id: getKPIid(productieStatus),
        datum: today,
        waarde: ids.join(','),
        site_id: null,
      }),
    );

    await safeCreateKPIWaarden(KPI_data_machinesPerProductieStatus);

  } catch (error) {
    console.error(`Fout bij het updaten van machine KPI's: ${error}`);
  }
};

