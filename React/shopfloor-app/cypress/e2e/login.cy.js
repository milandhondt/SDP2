describe('Login Page', () => {
  beforeEach(() => {
    cy.visit('http://localhost:5173');
  });

  it('should display the login form', () => {
    cy.get('[data-cy=loginEmail]').should('be.visible');
    cy.get('[data-cy=loginWachtwoord]').should('be.visible');
    cy.get('[data-cy=loginSubmitButton]').should('be.visible');
  });

  it('should show validation errors when submitting an empty form', () => {
    cy.get('[data-cy=loginEmail]').clear();
    cy.get('[data-cy=loginWachtwoord]').clear();
    cy.get('[data-cy=loginSubmitButton]').click();

    cy.contains('Email is verplicht').should('be.visible');
    cy.contains('Wachtwoord is verplicht').should('be.visible');
  });

  it('should login successfully as TECHNIEKER', () => {
    cy.loginAsTechnieker();
  });

  it('should login successfully as VERANTWOORDELIJKE', () => {
    cy.loginAsVerantwoordelijke();
  });

  it('should login successfully as MANAGER', () => {
    cy.loginAsManager();
  });

});
