describe('Machines Page', () => {
  beforeEach(() => {
    cy.fixture('machines.json').then((machinesData) => {
      cy.intercept('GET', 'http://localhost:9000/api/machines', { body: machinesData }).as('getMachines');
    });
    cy.loginAsManager();
    cy.visit('http://localhost:5173/machines');
    cy.wait('@getMachines');
  });

  it('should load and display machines from the fixture', () => {
    cy.get('table').should('exist');
    cy.get('tbody tr').should('have.length', 3);

    cy.get('tbody tr').eq(0).within(() => {
      cy.get('td').eq(0).should('contain', '1'); 
      cy.get('td').eq(1).should('contain', 'Alysson Road'); 
      cy.get('td').eq(2).should('contain', 'DRAAIT'); 
      cy.get('td').eq(3).should('contain', 'GEZOND'); 
      cy.get('td').eq(4).should('contain', 'Cormier Jodie'); 
      cy.get('td').eq(5).should('contain', '1'); 
    });

    cy.get('tbody tr').eq(1).within(() => {
      cy.get('td').eq(0).should('contain', '2'); 
      cy.get('td').eq(1).should('contain', 'Wehner Lights'); 
      cy.get('td').eq(2).should('contain', 'MANUEEL GESTOPT'); 
      cy.get('td').eq(3).should('contain', 'FALEND'); 
      cy.get('td').eq(4).should('contain', 'Jast Kyla'); 
      cy.get('td').eq(5).should('contain', '2'); 
    });

    cy.get('tbody tr').eq(2).within(() => {
      cy.get('td').eq(0).should('contain', '3'); 
      cy.get('td').eq(1).should('contain', 'Shanahan Pass'); 
      cy.get('td').eq(2).should('contain', 'IN ONDERHOUD'); 
      cy.get('td').eq(3).should('contain', 'GEZOND'); 
      cy.get('td').eq(4).should('contain', 'Kessler Haven'); 
      cy.get('td').eq(5).should('contain', '1'); 
    });
  });

  it('should show "Er zijn geen machines beschikbaar." when no machines exist', () => {
    cy.intercept('GET', 'http://localhost:9000/api/machines', { 
      body: { 
        items: [],
        total: 0, 
      }, 
    }).as('emptyMachines');

    cy.visit('http://localhost:5173/machines');
    cy.wait('@emptyMachines');

    cy.get('table').should('not.exist');
    cy.contains('Er zijn geen machines beschikbaar.').should('be.visible');
  });

  it('should allow sorting by columns', () => {
    // Test sorting by ID
    cy.get('th').contains('Nr.').click();
    cy.get('tbody tr').first().within(() => {
      cy.get('td').eq(0).should('contain', '3');
    });

    cy.get('th').contains('Nr.').click();
    cy.get('tbody tr').first().within(() => {
      cy.get('td').eq(0).should('contain', '1');
    });
    // Test sorting by Onderhoudsbeurten
    cy.get('th').contains('Aantal Onderhoudsbeurten').click();
    cy.get('tbody tr').first().within(() => {
      cy.get('td').eq(5).should('contain', '1');
    });

    cy.get('th').contains('Aantal Onderhoudsbeurten').click();
    cy.get('tbody tr').first().within(() => {
      cy.get('td').eq(5).should('contain', '2');
    });

  });

  it('should filter machines based on search query', () => {
    cy.get('[data-cy=search]').should('be.visible').clear().type('Road');
    cy.get('tbody tr').should('have.length', 1);
    cy.get('tbody tr td').eq(1).should('contain', 'Alysson Road');

    cy.get('[data-cy=search]').clear().type('Cormier');
    cy.get('tbody tr').should('have.length', 1);
    cy.get('tbody tr td').eq(4).should('contain', 'Cormier Jodie');

    cy.get('[data-cy=search]').clear().type('Nonexistent Machine');
    cy.get('tbody tr').should('have.length', 0);
    cy.contains('Er zijn geen machines beschikbaar.').should('be.visible');
  });

  it('should filter machines based on locatie', () => {
    cy.get('[data-cy=locatie_filter]').select('Alysson Road');
    cy.get('tbody tr').should('have.length', 1);
    cy.get('tbody tr td').eq(1).should('contain', 'Alysson Road');

    cy.get('[data-cy=locatie_filter]').select('Alle locaties');
    cy.get('tbody tr').should('have.length', 3);
  });

  it('should filter machines based on status', () => {
    cy.get('[data-cy=status_filter]').select('DRAAIT');
    cy.get('tbody tr').should('have.length', 1);
    cy.get('tbody tr td').eq(2).should('contain', 'DRAAIT');

    cy.get('[data-cy=status_filter]').select('Alle statussen');
    cy.get('tbody tr').should('have.length', 3);
  });

  it('should filter machines based on productie status', () => {
    cy.get('[data-cy=productie_status_filter]').select('GEZOND');
    cy.get('tbody tr').should('have.length', 2);
    cy.get('tbody tr td').eq(3).should('contain', 'GEZOND');

    cy.get('[data-cy=productie_status_filter]').select('Alle productie statussen');
    cy.get('tbody tr').should('have.length', 3);
  });

  it('should filter machines based on technieker', () => {
    cy.get('[data-cy=technieker_filter]').select('Cormier Jodie');
    cy.get('tbody tr').should('have.length', 1);
    cy.get('tbody tr td').eq(4).should('contain', 'Cormier Jodie');

    cy.get('[data-cy=technieker_filter]').select('Alle techniekers');
    cy.get('tbody tr').should('have.length', 3);
  });

  it('should reset all filters when "Filters wissen" is clicked', () => {
    // Apply some filters
    cy.get('[data-cy=locatie_filter]').select('Alysson Road');
    cy.get('[data-cy=status_filter]').select('DRAAIT');
    cy.get('[data-cy=productie_status_filter]').select('GEZOND');
    cy.get('[data-cy=technieker_filter]').select('Cormier Jodie');

    // Check that filters are applied
    cy.get('tbody tr').should('have.length', 1);

    // Click reset filters
    cy.get('[data-cy=reset_filters]').click();

    // Verify all filters are reset
    cy.get('[data-cy=locatie_filter]').should('have.value', '');
    cy.get('[data-cy=status_filter]').should('have.value', '');
    cy.get('[data-cy=productie_status_filter]').should('have.value', '');
    cy.get('[data-cy=technieker_filter]').should('have.value', '');
    cy.get('[data-cy=search]').should('have.value', '');
    cy.get('tbody tr').should('have.length', 3);
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
});