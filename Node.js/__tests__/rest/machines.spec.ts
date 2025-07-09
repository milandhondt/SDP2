import type supertest from 'supertest';
import withServer from '../helpers/withServer';
import { prisma } from '../../src/data';
import { Status } from '@prisma/client';
import { Machine_Status, Productie_Status } from '@prisma/client';
import Role from '../../src/core/roles';
import { loginAdmin } from '../helpers/login';

jest.setTimeout(20000);

describe('Machines API', () => {
  let request: supertest.Agent;
  let adminAuthHeader: string;

  withServer((r) => (request = r));

  beforeAll(async () => {
    adminAuthHeader = await loginAdmin(request);
  });

  const url = '/api/machines';

  let createdMachine: any;

  beforeAll(async () => {
    await prisma.notificatie.deleteMany({});
    await prisma.kPIWaarde.deleteMany({});
    await prisma.kPI.deleteMany({});
    await prisma.machine.deleteMany({});
    await prisma.site.deleteMany({});
    await prisma.gebruiker.deleteMany({});
    await prisma.adres.deleteMany({});

    await prisma.adres.create({
      data: {
        id: 1,
        street: 'Teststraat',
        number: '1A',
        city: 'Teststad',
        postalcode: '1234',
        land: 'Testland',
      },
    });

    await prisma.gebruiker.create({
      data: {
        id: 1,
        lastname: 'Test User',
        firstname: 'Test',
        birthdate: new Date(1990, 1, 1),
        email: 'user@test.com',
        password: 'password',
        phonenumber: '1234567890',
        role: Role.TECHNIEKER,
        status: Status.ACTIEF,
        address_id: 1,
      },
    });

    await prisma.site.create({
      data: {
        id: 1,
        sitename: 'Test Site',
        verantwoordelijke_id: 1,
        status: Status.ACTIEF,
      },
    });

    createdMachine = await prisma.machine.create({
      data: {
        code: 'MACHINE123',
        location: 'Test Location',
        machinestatus: Machine_Status.DRAAIT,
        productionstatus: Productie_Status.GEZOND,
        site_id: 1,
        product_naam: 'Apple M2',
        productinfo: '2.95 GHz processor',
        technician_id: 1,
        aantal_goede_producten: 596,
        lastmaintenance: '2025-03-11T08:36:39.975Z',
        aantal_slechte_producten: 678,
        limiet_voor_onderhoud: 21407,
      },
    });

    const createdKPI = await prisma.kPI.create({
      data: {
        onderwerp: 'Productie Efficiëntie',
        roles: {
          admin: true,
          user: false,
        },
        grafiek: 'LINE',
      },
    });

    await prisma.kPIWaarde.create({
      data: {
        datum: new Date(),
        waarde: { score: 90 },
        site_id: '1',
        kpi_id: createdKPI.id,
      },
    });
  });

  afterAll(async () => {
    await prisma.kPIWaarde.deleteMany({});
    await prisma.kPI.deleteMany({});
  });

  it('should return 403 for a non-existent machine', async () => {
    const response = await request
      .get(`${url}/9999`)
      .set('Authorization', adminAuthHeader);
    expect(response.status).toBe(403);
    expect(response.body.message).toBe('Machine niet gevonden');
  });

  it('should return 401 for a missing or invalid Authorization header', async () => {
    const response = await request
      .get(`${url}/${createdMachine.id}`)
      .set('Authorization', 'InvalidToken');
    expect(response.status).toBe(401);
    expect(response.body.message).toBe('Invalid authentication token');
  });

  it('should return 401 when not authorized', async () => {
    const response = await request.get(url);
    expect(response.status).toBe(401);
    expect(response.body.message).toBe('You need to be signed in');
  });

  it('should return 500 if the database is unavailable', async () => {
    jest.spyOn(prisma.machine, 'findMany').mockImplementationOnce(() => {
      throw new Error('Database error');
    });

    const response = await request.get(url).set('Authorization', adminAuthHeader);
    expect(response.status).toBe(500);
    expect(response.body.message).toBe('Database error');
  });

  it('should get a machine by ID (GET)', async () => {
    const response = await request
      .get(`${url}/${createdMachine.id}`)
      .set('Authorization', adminAuthHeader);

    expect(response.status).toBe(200);
    expect(response.body.id).toBe(createdMachine.id);
  });
});
