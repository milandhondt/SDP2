# 2025-react-gent12
Groepsleden:
- Robin Ledoux [robin.ledoux@student.hogent.be](mailto:robin.ledoux@student.hogent.be)
- Milan Dhondt [milan.dhondt@student.hogent.be](mailto:milan.dhondt@student.hogent.be)
- Dogukan Uyanik [dogukan.uyanik@student.hogent.be](mailto:dogukan.uyanik@student.hogent.be)
- Sijad Walipoor [sijad.walipoor@student.hogent.be](mailto:sijad.walipoor@student.hogent.be)
- Dylan De Man [dylan.deman@student.hogent.be](mailto:dylan.deman@student.hogent.be)

Inloggegevens testaccounts:

## Administrator
alice.admin@example.com
123456789

## Manager
bob.manager@example.com
123456789

## Verantwoordelijke
charlie.verantwoordelijke@example.com
123456789

## Technieker
david.technieker@example.com
123456789

Installeer alle dependencies met het volgende commando:

```bash
yarn install
```

Maak een `.env` met de volgende inhoud en pas aan aan uw eigen configuratie:

```dotenv
VITE_API_URL=http://localhost:9000/api
```

## Start de app

### Development

- Zorg voor een `.env`
- Start de app met `yarn dev`. Het runt standaard op <http://localhost:5137>

### Productie

- Zorg voor een `.env`
- Build de app met `yarn build`. Dit genereert een `dist` folder met de gecompileerde bestanden.
- Laad deze folder op bij een service zoals Apache of Nginx.

## Test de app

Run de testen met `yarn test` en kies voor `E2E testing` in het Cypress venster. Het gaat een nieuwe browser venster openen waar u kan kiezen welke test suite u wil runnen.
