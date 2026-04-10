import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Prenotazioni e2e test', () => {
  const prenotazioniPageUrl = '/prenotazioni';
  const prenotazioniPageUrlPattern = new RegExp('/prenotazioni(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const prenotazioniSample = { data: '2026-01-21', oraInizio: '04:54:00', oraFine: '03:34:00' };

  let prenotazioni;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/prenotazionis+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/prenotazionis').as('postEntityRequest');
    cy.intercept('DELETE', '/api/prenotazionis/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (prenotazioni) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/prenotazionis/${prenotazioni.id}`,
      }).then(() => {
        prenotazioni = undefined;
      });
    }
  });

  it('Prenotazionis menu should load Prenotazionis page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('prenotazioni');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Prenotazioni').should('exist');
    cy.url().should('match', prenotazioniPageUrlPattern);
  });

  describe('Prenotazioni page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(prenotazioniPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Prenotazioni page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/prenotazioni/new$'));
        cy.getEntityCreateUpdateHeading('Prenotazioni');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prenotazioniPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/prenotazionis',
          body: prenotazioniSample,
        }).then(({ body }) => {
          prenotazioni = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/prenotazionis+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/prenotazionis?page=0&size=20>; rel="last",<http://localhost/api/prenotazionis?page=0&size=20>; rel="first"',
              },
              body: [prenotazioni],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(prenotazioniPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Prenotazioni page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('prenotazioni');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prenotazioniPageUrlPattern);
      });

      it('edit button click should load edit Prenotazioni page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Prenotazioni');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prenotazioniPageUrlPattern);
      });

      it('edit button click should load edit Prenotazioni page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Prenotazioni');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prenotazioniPageUrlPattern);
      });

      it('last delete button click should delete instance of Prenotazioni', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('prenotazioni').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', prenotazioniPageUrlPattern);

        prenotazioni = undefined;
      });
    });
  });

  describe('new Prenotazioni page', () => {
    beforeEach(() => {
      cy.visit(`${prenotazioniPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Prenotazioni');
    });

    it('should create an instance of Prenotazioni', () => {
      cy.get(`[data-cy="data"]`).type('2026-01-21');
      cy.get(`[data-cy="data"]`).blur();
      cy.get(`[data-cy="data"]`).should('have.value', '2026-01-21');

      cy.get(`[data-cy="oraInizio"]`).type('21:45:00');
      cy.get(`[data-cy="oraInizio"]`).invoke('val').should('match', new RegExp('21:45:00'));

      cy.get(`[data-cy="oraFine"]`).type('17:37:00');
      cy.get(`[data-cy="oraFine"]`).invoke('val').should('match', new RegExp('17:37:00'));

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        prenotazioni = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', prenotazioniPageUrlPattern);
    });
  });
});
