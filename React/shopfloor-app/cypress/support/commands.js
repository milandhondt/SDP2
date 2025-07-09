// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })

Cypress.Commands.add('login', (email, password) => {
  cy.visit('http://localhost:5173');

  // Intercept the correct login request
  cy.intercept('POST', 'http://localhost:9000/api/sessions').as('loginRequest');

  cy.get('[data-cy=loginEmail]').clear();
  if (email) cy.get('[data-cy=loginEmail]').type(email);

  cy.get('[data-cy=loginWachtwoord]').clear();
  if (password) cy.get('[data-cy=loginWachtwoord]').type(password);

  cy.get('[data-cy=loginSubmitButton]').click();

  cy.wait('@loginRequest').then((interception) => {
    expect(interception.response.statusCode).to.eq(200);

    const token = interception.response.body.token;
    if (token) {
      cy.setCookie('auth_token', token);
      localStorage.setItem('auth_token', token);
    }
  });
});

Cypress.Commands.add('loginAsManager', () => {
  cy.login('bob.manager@example.com', '123456789');
});

Cypress.Commands.add('loginAsVerantwoordelijke', () => {
  cy.login('charlie.verantwoordelijke@example.com', '123456789');
});

Cypress.Commands.add('loginAsTechnieker', () => {
  cy.login('david.technieker@example.com', '123456789');
});

