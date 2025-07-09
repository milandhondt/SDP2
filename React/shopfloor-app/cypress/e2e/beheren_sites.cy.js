describe('SiteToevoegen Page Tests', () => {
  beforeEach(() => {
    cy.intercept('POST', 'http://localhost:9000/api/sessions').as('loginRequest');
    cy.intercept('GET', 'http://localhost:9000/api/users/me').as('getUser');
    cy.intercept('POST', 'http://localhost:9000/api/sites', { statusCode: 201 }).as('createSite');

    cy.loginAsManager();
    cy.wait('@loginRequest');
    cy.wait('@getUser');

    cy.visit('http://localhost:5173/sites');
    cy.get('[data-cy="add-site-button"]').click();
    cy.url().should('include', '/sites/new');
    cy.contains('Nieuwe site toevoegen').should('be.visible');
  }); 

  it('should select a verantwoordelijke and fill in the form', () => {
    cy.get('[data-cy="site-name"]').type('Test Site');
    cy.get('[data-cy="verantwoordelijke-select"]').select('Davis');
    cy.get('[data-cy="status-select"]').select('INACTIEF');
  });

  it('should submit the form successfully', () => {
    cy.get('[data-cy="site-name"]').debug().type('Test Site');
    cy.get('[data-cy="verantwoordelijke-select"]').select('Davis');
    cy.get('[data-cy="submit-button"]').click();
    cy.wait('@createSite');
    cy.contains('Site succesvol toegevoegd!').should('be.visible');
  });

  it('should show an error message on failed submission', () => {
    cy.intercept('POST', 'http://localhost:9000/api/sites', { statusCode: 400 }).as('createSiteFail');
    cy.get('[data-cy="site-name"]').type('Test Site');
    cy.get('[data-cy="verantwoordelijke-select"]').select('Davis');
    cy.get('[data-cy="submit-button"]').click();
    cy.wait('@createSiteFail');
    cy.contains('Er is een fout opgetreden').should('be.visible');
  });
}); 

describe('SiteEdit Page Tests', () => {
  beforeEach(() => {
    cy.intercept('POST', 'http://localhost:9000/api/sessions').as('loginRequest');
    cy.intercept('GET', 'http://localhost:9000/api/users/me').as('getUser');
    cy.intercept('PUT', 'http://localhost:9000/api/sites/*', { statusCode: 200 }).as('updateSite');
    cy.fixture('sites.json').then((sitesData) => {
      cy.intercept('GET', 'http://localhost:9000/api/sites', { body: sitesData }).as('getSites');
    });
    cy.fixture('siteDetails.json').then((data) => {
      cy.intercept('GET', 'http://localhost:9000/api/sites/1', { body: data }).as('getSite');
    });

    cy.loginAsManager();
    cy.wait('@loginRequest');
    cy.wait('@getUser');

    cy.visit('http://localhost:5173/sites');
    cy.wait('@getSites');
    cy.get('tr').contains('Site A').parent('tr').find('[data-cy="edit-button"]').click();
    
    cy.wait('@getSite');
  });

  it('should load the page with the existing site data', () => {
    cy.get('[data-cy="site-name"]').should('have.value', 'Site A'); 
    cy.get('[data-cy="verantwoordelijke-select"]').should('have.value', 'Selecteer verantwoordelijke'); 
    cy.get('[data-cy="status-select"]').should('have.value', 'ACTIEF'); 
  });

  it('should allow editing the site details', () => {
    cy.get('[data-cy="site-name"]').clear();
    cy.get('[data-cy="site-name"]').type('Updated Test Site');
    cy.get('[data-cy="verantwoordelijke-select"]').select('Davis'); 
    cy.get('[data-cy="status-select"]').select('INACTIEF'); 

    cy.get('[data-cy="site-name"]').should('have.value', 'Updated Test Site');
    cy.get('[data-cy="verantwoordelijke-select"]').should('have.value', '4');
    cy.get('[data-cy="status-select"]').should('have.value', 'INACTIEF');
  });

  it('should submit the edited site successfully', () => {
    cy.get('[data-cy="site-name"]').clear();
    cy.get('[data-cy="site-name"]').type('Updated Test Site');
    cy.get('[data-cy="verantwoordelijke-select"]').select('Davis');

    cy.get('[data-cy="submit-button"]').click();

    cy.contains('Site succesvol bijgewerkt!').should('be.visible');
    cy.wait('@updateSite');
  });

  it('should show an error message when the update fails', () => {
    cy.intercept('PUT', 'http://localhost:9000/api/sites/*', { statusCode: 400 }).as('updateSiteFail');

    cy.get('[data-cy="site-name"]').clear();
    cy.get('[data-cy="site-name"]').type('Updated Test Site');
    cy.get('[data-cy="verantwoordelijke-select"]').select('Davis');
    cy.get('[data-cy="submit-button"]').click();

    cy.wait('@updateSiteFail');
    cy.contains('Er is een fout opgetreden bij het bijwerken van de site').should('be.visible');
  });
});