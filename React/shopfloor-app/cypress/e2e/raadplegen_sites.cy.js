describe('Sites Page', () => {
  describe('As MANAGER', () => {
    beforeEach(() => {
      cy.fixture('sites.json').then((sitesData) => {
        cy.intercept('GET', 'http://localhost:9000/api/sites', { body: sitesData }).as('getSites');
      });
      cy.loginAsManager();
      cy.visit('http://localhost:5173/sites');
      cy.wait('@getSites');
    });

    it('should show add site button', () => {
      cy.get('[data-cy=add-site-button]').should('be.visible');
    });

    it('should allow adding new sites', () => {
      cy.get('[data-cy=add-site-button]').click();
      cy.url().should('include', '/sites/new');
    });

    it('should show edit option in table', () => {
      cy.get('[data-cy=edit-button]').first().should('be.visible');
    });
  }); 
  
  describe('As VERANTWOORDELIJKE', () => {
    beforeEach(() => {
      cy.fixture('sites.json').then((sitesData) => {
        cy.intercept('GET', 'http://localhost:9000/api/sites', { body: sitesData }).as('getSites');
      });
      cy.loginAsVerantwoordelijke();
      cy.visit('http://localhost:5173/sites');
      cy.wait('@getSites');
    });

    it('should load and display sites from the fixture', () => {
      cy.get('table').should('exist');
      cy.get('tbody tr').should('have.length', 3);

      cy.get('tbody tr').eq(0).within(() => {
        cy.get('td').eq(0).should('contain', '1');
        cy.get('td').eq(1).should('contain', 'Site A');
        cy.get('td').eq(2).should('contain', 'Jan Janssen');
        cy.get('td').eq(3).should('contain', 'ACTIEF');
        cy.get('td').eq(4).should('contain', '2');
      });
    });

    it('should show "Er zijn geen sites beschikbaar." when no sites exist', () => {
      cy.intercept('GET', 'http://localhost:9000/api/sites', { body: { items: [] } }).as('emptySites');
      cy.visit('http://localhost:5173/sites');
      cy.wait('@emptySites');

      cy.get('table').should('not.exist');
      cy.contains('Er zijn geen sites beschikbaar.').should('be.visible');
    });

    it('should filter sites based on verantwoordelijke', () => {
      cy.get('[data-cy=verantwoordelijke_filter]').select('Jan Janssen');
      cy.get('tbody tr').should('have.length', 1);
      cy.get('[data-cy=verantwoordelijke_filter]').select('Alle verantwoordelijken');
      cy.get('tbody tr').should('have.length', 3);
    });

    it('should not show add site button', () => {
      cy.get('[data-cy=add-site-button]').should('not.exist');
    });
  });

  describe('As TECHNIEKER', () => {
    beforeEach(() => {
      cy.fixture('sites.json').then((sitesData) => {
        cy.intercept('GET', 'http://localhost:9000/api/sites', { body: sitesData }).as('getSites');
      });
      cy.loginAsTechnieker();
      cy.visit('http://localhost:5173/sites');
      cy.wait('@getSites');
    });

    it('should only show assigned sites', () => {
      cy.get('tbody tr').should('have.length.at.least', 1);
    });

    it('should not show management options', () => {
      cy.get('[data-cy=add-site-button]').should('not.exist');
      cy.get('[data-cy=edit-button]').should('not.exist');
    });
  });

  describe('Common Functionality', () => {
    beforeEach(() => {
      cy.fixture('sites.json').then((sitesData) => {
        cy.intercept('GET', 'http://localhost:9000/api/sites', { body: sitesData }).as('getSites');
      });
      cy.loginAsVerantwoordelijke();
      cy.visit('http://localhost:5173/sites');
      cy.wait('@getSites');
    });

    it('should filter sites based on aantal machines', () => {
      cy.get('[data-cy=machines_filter_min]').clear();
      cy.get('[data-cy=machines_filter_min]').type('2');
      cy.get('tbody tr').should('have.length', 1);

      cy.get('[data-cy=machines_filter_min]').clear();
      cy.get('[data-cy=machines_filter_max]').clear();
      cy.get('[data-cy=machines_filter_max]').type('1');
      cy.get('tbody tr').should('have.length', 2);
    });

    it('should update pagination and number of results per page', () => {
      cy.get('[data-cy=page_size]').should('have.value', '10');
      cy.get('[data-cy=page_size]').select('5');
      cy.get('tbody tr').should('have.length.lte', 5);
      cy.get('[data-cy=next_page]').should('exist');
      cy.get('[data-cy=previous_page]').should('exist');
    });
  
    it('should filter sites based on search query', () => {
      
      // cy.get('[data-cy=search]').should('be.visible').clear().type('Site A');
      // cy.get('tbody tr').should('have.length', 1);
      // cy.get('tbody tr td').eq(1).should('contain', 'Site A');
  
      cy.get('[data-cy=search]').clear().type('Jan Janssen');
      cy.get('tbody tr').should('have.length', 1);
      cy.get('tbody tr td').eq(2).should('contain', 'Jan Janssen');
  
      cy.get('[data-cy=search]').clear().type('Nonexistent Site');
      cy.get('tbody tr').should('have.length', 0);
      cy.contains('Er zijn geen sites beschikbaar.').should('be.visible');
    });
  
    it('should filter sites based on status', () => {
      cy.get('[data-cy=status_filter]').select('ACTIEF');
      cy.get('tbody tr').should('have.length', 2);
  
      cy.get('[data-cy=status_filter]').select('Alle statussen');
      cy.get('tbody tr').should('have.length', 3);
  
      cy.get('[data-cy=status_filter]').select('INACTIEF');
      cy.get('tbody tr').should('have.length', 1);
    },
    );
    
    it('should allow sorting by Aantal Machines', () => {
      cy.get('th').contains('Aantal machines').click();
  
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(4).should('contain', '1'); 
      });
  
      cy.get('th').contains('Aantal machines').click(); 
  
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(4).should('contain', '2'); 
      });
    });

    it('should allow sorting by Verantwoordelijke', () => {
      cy.get('th').contains('Verantwoordelijke').click();
  
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(2).should('contain', 'Jan Janssen'); 
      });
  
      cy.get('th').contains('Verantwoordelijke').click(); 
  
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(2).should('contain', 'Piet Peeters'); 
      });
    });

    it('should allow sorting by Status', () => {
      cy.get('th').contains('Status').click();
  
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(3).should('contain', 'ACTIEF'); 
      });
  
      cy.get('th').contains('Status').click(); 
  
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(3).should('contain', 'INACTIEF'); 
      });
    });
  });
});