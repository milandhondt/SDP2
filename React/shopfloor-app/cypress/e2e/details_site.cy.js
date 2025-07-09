describe('Site Details Page', () => {
  beforeEach(() => {
    cy.fixture('siteDetails.json').then((siteData) => {
      cy.intercept('GET', 'http://localhost:9000/api/sites/1', { body: siteData }).as('getSiteDetails');
    });
    cy.loginAsVerantwoordelijke();
    cy.visit('http://localhost:5173/sites/1');
    cy.wait('@getSiteDetails');
  });

  it('should display site details correctly', () => {
    cy.get('[data-cy=site-details]').should('be.visible');
  });

});

describe('Site Grondplan Page', () => {
  beforeEach(() => {
    cy.intercept('POST', 'http://localhost:9000/api/sessions').as('loginRequest');
    cy.intercept('GET', 'http://localhost:9000/api/users/me').as('getUser');
    cy.login('robert.devree@hotmail.com', '123456789');

    cy.intercept('GET', 'http://localhost:9000/api/sites/1', { fixture: 'siteDetails.json' }).as('getSiteDetails');
    cy.visit('http://localhost:5173/sites/1/grondplan');
    cy.wait('@getSiteDetails', { timeout: 10000 });
  });

  it('should display the map correctly', () => {
    cy.get('[data-cy=map]').should('be.visible');
  });

  it('should show machine details on clicking a machine marker', () => {
    cy.get('[data-cy=machine-marker]').first().click();
    cy.get('[data-cy=machine-details]').should('be.visible');
  });

  it('should show an error if the map fails to load', () => {
    cy.intercept('GET', 'http://localhost:9000/api/sites/1', { statusCode: 500 }).as('getSiteDetailsError');
    cy.visit('http://localhost:5173/sites/1/grondplan');
    cy.wait('@getSiteDetailsError');
    cy.get('[data-cy=error-message]').should('be.visible');
  });
});

describe('Machine Details Page', () => {
  beforeEach(() => {
    cy.intercept('POST', 'http://localhost:9000/api/sessions').as('loginRequest');
    cy.intercept('GET', 'http://localhost:9000/api/users/me').as('getUser');
    cy.login('robert.devree@hotmail.com', '123456789');

    cy.fixture('machineDetails.json').then((fixtureData) => {
      cy.intercept('GET', 'http://localhost:9000/api/machines/1', (req) => {
        req.reply(fixtureData.machines[0]);
      }).as('getMachineDetails');
    });

    cy.visit('http://localhost:5173/machines/1');
    cy.wait('@getMachineDetails');
  });

  it('should display machine details correctly', () => {
    cy.get('[data-cy=machine_details]').should('be.visible');
    cy.get('[data-cy=machine_status]').should('be.visible');
    cy.get('[data-cy=machine_productie_status]').should('be.visible');
    cy.get('[data-cy=machine_details]').should('contain', 'Machine informatie');
    cy.get('[data-cy=machine_status]').should('contain', 'Status: DRAAIT');
    cy.get('[data-cy=machine_productie_status]').should('contain', 'FALEND');
  });

  // Negative test cases
  it('should display an error message when machine details fail to load', () => {
    cy.intercept('GET', 'http://localhost:9000/api/machines/1', { statusCode: 500 }).as('getMachineDetailsError');
    cy.visit('http://localhost:5173/machines/1');
    cy.wait('@getMachineDetailsError');
    cy.get('[data-cy=error-message]').should('be.visible');
  });

  it('should display a not found message when machine does not exist', () => {
    cy.intercept('GET', 'http://localhost:9000/api/machines/1', { statusCode: 404 }).as('getMachineNotFound');
    cy.visit('http://localhost:5173/machines/1');
    cy.wait('@getMachineNotFound');
    cy.get('[data-cy=error-message]').should('be.visible');
  });
});

describe('Machine Start and Stop E2E Tests', () => {
  let machineData;

  beforeEach(() => {
    cy.loginAsVerantwoordelijke();
    cy.intercept('GET', 'http://localhost:9000/api/machines/1', { fixture: 'machineDetails.json' })
      .as('getMachineDetails');

    cy.fixture('machineDetails.json').then((data) => {
      machineData = data; // Store the fixture data to use in the tests

      cy.intercept('GET', 'http://localhost:9000/api/machines/1', {
        statusCode: 200,
        body: machineData.machines[0],
      }).as('getMachineA');
      
      cy.intercept('GET', 'http://localhost:9000/api/machines/2', {
        statusCode: 200,
        body: machineData.machines[1],
      }).as('getMachineB');

      // Mock machine status update API with full payload validation
      cy.intercept('PUT', 'http://localhost:9000/api/machines/*', (req) => {
        // Validate required fields
        const requiredFields = [
          'site_id', 
          'technieker_id', 
          'code', 
          'locatie', 
          'status', 
          'productie_status',
        ];

        // Check that all required fields are present
        const missingFields = requiredFields.filter((field) => !req.body[field]);

        if (missingFields.length > 0) {
          return req.reply({
            statusCode: 400,
            body: { 
              error: 'Missing required fields', 
              missingFields: missingFields, 
            },
          });
        }

        // Validate status
        const validStatuses = ['DRAAIT', 'MANUEEL_GESTOPT', 'IN_ONDERHOUD', 'AUTOMATISCH_GESTOPT', 'STARTBAAR'];
        const validProductieStatuses = ['GEZOND', 'NOOD_ONDERHOUD', 'FALEND'];

        if (!validStatuses.includes(req.body.status)) {
          return req.reply({
            statusCode: 400,
            body: { error: 'Invalid status' },
          });
        }

        if (!validProductieStatuses.includes(req.body.productie_status)) {
          return req.reply({
            statusCode: 400,
            body: { error: 'Invalid productie status' },
          });
        }

        // If all validations pass, simulate successful update
        req.reply({
          statusCode: 200,
          body: {
            ...req.body,
            status: req.body.status,
          },
        });
      }).as('updateMachineStatus');
      
      cy.visit('http://localhost:5173/machines/1');
      cy.wait('@getMachineA');
    });
  });

  // it('should stop a running machine', () => {
  //   cy.get('[data-cy=machine_status]').should('contain.text', 'Status: DRAAIT');

  //   cy.get('[data-cy="start-stop-button"]').contains('STOP').click();

  //   cy.wait('@updateMachineStatus');

  //   cy.intercept('GET', 'http://localhost:9000/api/machines/1', {
  //     statusCode: 200,
  //     body: {
  //       ...machineData.machines[0], // Keep all fields from fixture
  //       status: 'MANUEEL_GESTOPT',   // Updated status
  //     },
  //   }).as('getUpdatedMachine');

  //   cy.visit('http://localhost:5173/machines/1');
  //   cy.wait('@getUpdatedMachine');

  //   cy.get('[data-cy="start-stop-button"]')
  //     .contains('START');

  //   cy.get('[data-cy="machine_status"]').should('contain.text', 'Status: MANUEEL GESTOPT');
  // });

  // it('should start a stopped machine', () => {
  //   cy.visit('http://localhost:5173/machines/2');
  //   cy.wait('@getMachineB');

  //   cy.get('[data-cy="machine_status"]').should('contain.text', 'MANUEEL GESTOPT');

  //   cy.get('[data-cy="start-stop-button"]').contains('START').click();

  //   cy.wait('@updateMachineStatus');

  //   cy.intercept('GET', 'http://localhost:9000/api/machines/2', {
  //     statusCode: 200,
  //     body: {
  //       ...machineData.machines[1], 
  //       status: 'DRAAIT',          
  //     },
  //   }).as('getUpdatedMachine');

  //   cy.visit('http://localhost:5173/machines/2');
  //   cy.wait('@getUpdatedMachine');

  //   cy.get('[data-cy="start-stop-button"]')
  //     .contains('STOP');

  //   cy.get('[data-cy="machine_details"]').should('contain.text', 'DRAAIT');
  // });

  it('should show a 404 error when the machine is not found', () => {
    cy.intercept('GET', 'http://localhost:9000/api/machines/999', {
      statusCode: 404,
      body: { error: 'Machine not found' },
    }).as('getMachineNotFound');

    cy.visit('http://localhost:5173/machines/999');
    cy.wait('@getMachineNotFound');

    cy.get('[data-cy="error-message"]').should('be.visible').and('contain.text', 'Oeps, er is iets mis gegaan...');
  });
});