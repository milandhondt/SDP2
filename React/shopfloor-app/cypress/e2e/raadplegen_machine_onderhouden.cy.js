describe('Onderhoud List Page', () => {
  describe('As TECHNIEKER', () => {
    beforeEach(() => {
      cy.fixture('machineDetails.json').then((fixtureData) => {
        cy.intercept('GET', 'http://localhost:9000/api/machines/1', (req) => {
          req.reply(fixtureData.machines[0]);
        }).as('getMachineDetails');
      });

      cy.loginAsTechnieker();
      cy.visit('http://localhost:5173/machines/1/onderhouden');
      cy.wait('@getMachineDetails');
    });

    it('should only show assigned onderhouden of his machine', () => {
      cy.get('tbody tr').should('have.length.at.least', 1);
      cy.get('tbody tr').eq(0).within(() => {
        cy.get('td').eq(0).should('contain', '95');
        cy.get('td').eq(1).should('contain', '2024-06-21T19:00:11.000Z');
        cy.get('td').eq(2).should('contain', '2025-11-14T13:10:34.000Z');
        cy.get('td').eq(3).should('contain', 'Gerlach Alessia');
        cy.get('td').eq(4).should('contain', 'Degero caritas vomica quidem cogo amplus conscendo.');
        cy.get('td').eq(7).should('contain', 'VOLTOOID');
      });
    });
  });

  describe('Common Functionality', () => {
    beforeEach(() => {
      cy.fixture('machineDetails.json').then((fixtureData) => {
        cy.intercept('GET', 'http://localhost:9000/api/machines/1', (req) => {
          req.reply(fixtureData.machines[0]);
        }).as('getMachineDetails');
      });

      cy.loginAsVerantwoordelijke();
      cy.visit('http://localhost:5173/machines/1/onderhouden');
      cy.wait('@getMachineDetails');
    });

    it('should load and display onderhouden from the fixture', () => {
      cy.get('table').should('exist');
      cy.get('tbody tr').should('have.length', 1);
  
      // Check first row details
      cy.get('tbody tr').eq(0).within(() => {
        cy.get('td').eq(0).should('contain', '95');
        cy.get('td').eq(1).should('contain', '2024-06-21T19:00:11.000Z');
        cy.get('td').eq(2).should('contain', '2025-11-14T13:10:34.000Z');
        cy.get('td').eq(3).should('contain', 'Gerlach Alessia');
        cy.get('td').eq(4).should('contain', 'Degero caritas vomica quidem cogo amplus conscendo.');
        cy.get('td').eq(5).should('contain', 'Itaque coepi unde cubo tergeo tametsi creator aureus. Attollo conicio adimpleo amplitudo agnosco ater. Blandior conitor illo virgo adaugeo ubi tamen vulgaris cernuus iusto.');
        cy.get('td').eq(7).should('contain', 'VOLTOOID');
      });
    });
  
    it('should allow sorting by columns', () => {
      // Test sorting by ID
      cy.get('th').contains('Nr.').click();
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(0).should('contain', '95');
      });
  
      cy.get('th').contains('Nr.').click();
      cy.get('tbody tr').first().within(() => {
        cy.get('td').eq(0).should('contain', '95');
      });
    });
  
    it('should filter onderhouden based on search query', () => {
      cy.get('[data-cy=search]').should('be.visible').clear().type('Gerlach');
      cy.get('tbody tr').should('have.length', 1);
      cy.get('tbody tr td').eq(3).should('contain', 'Gerlach Alessia');
  
      cy.get('[data-cy=search]').clear().type('Degero');
      cy.get('tbody tr').should('have.length', 1);
      cy.get('tbody tr td').eq(4).should('contain', 'Degero caritas vomica quidem cogo amplus conscendo.');
  
      cy.get('[data-cy=search]').clear().type('Nonexistent');
      cy.get('tbody tr').should('have.length', 0);
      cy.contains('Er zijn geen onderhouden beschikbaar voor deze machine.').should('be.visible');
    });
  
    it('should filter onderhouden based on status', () => {
      cy.get('[data-cy=status_filter]').select('VOLTOOID');
      cy.get('tbody tr').should('have.length', 1);
      cy.get('tbody tr td').eq(7).should('contain', 'VOLTOOID');
  
      cy.get('[data-cy=status_filter]').select('');
      cy.get('tbody tr').should('have.length', 1);
    });
  
    it('should filter onderhouden based on technieker', () => {
      cy.get('[data-cy=onderhoud_technieker_filter]').select('Gerlach Alessia');
      cy.get('tbody tr').should('have.length', 1);
      cy.get('tbody tr td').eq(3).should('contain', 'Gerlach Alessia');
  
      cy.get('[data-cy=onderhoud_technieker_filter]').select('');
      cy.get('tbody tr').should('have.length', 1);
    });
  
    it('should reset all filters when "Filters wissen" is clicked', () => {
      // Apply filters
      cy.get('[data-cy=status_filter]').select('VOLTOOID');
      cy.get('[data-cy=onderhoud_technieker_filter]').select('Gerlach Alessia');
  
      // Check that filters are applied
      cy.get('tbody tr').should('have.length', 1);
  
      // Click reset filters
      cy.get('[data-cy=reset_filters]').click();
  
      // Verify all filters are reset
      cy.get('[data-cy=status_filter]').should('have.value', '');
      cy.get('[data-cy=onderhoud_technieker_filter]').should('have.value', '');
      cy.get('tbody tr').should('have.length', 1);
    });
  
    it('should update pagination and number of results per page', () => {
      // Default should be 10 per page
      cy.get('[data-cy=page_size]').should('have.value', '10');
  
      // Change page size to 5
      cy.get('[data-cy=page_size]').select('5');
      cy.get('tbody tr').should('have.length.lte', 5);
  
      // Navigate through pages if more than 5 machines
      cy.get('[data-cy=next_page]').should('exist');
      cy.get('[data-cy=previous_page]').should('exist');
    });
  
    it('should have correct unique statuses and techniekers', () => {
      // Verify unique statuses
      cy.get('[data-cy=status_filter] option').should('have.length.at.least', 2);
      cy.get('[data-cy=status_filter]').within(() => {
        cy.contains('VOLTOOID').should('exist');
        cy.contains('Alle statussen').should('exist');
      });
  
      // Verify unique techniekers
      cy.get('[data-cy=onderhoud_technieker_filter] option').should('have.length.at.least', 1);
      cy.get('[data-cy=onderhoud_technieker_filter]').within(() => {
        cy.contains('Gerlach Alessia').should('exist');
      });
    });
  });
});