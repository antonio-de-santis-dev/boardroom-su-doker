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

describe('StatiPrenotazione e2e test', () => {
  const statiPrenotazionePageUrl = '/stati-prenotazione';
  const statiPrenotazionePageUrlPattern = new RegExp('/stati-prenotazione(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const statiPrenotazioneSample = { descrizione: 'if zowie provided', codice: 'CANCELLED', ordineAzione: 23405 };

  let statiPrenotazione;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/stati-prenotaziones+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/stati-prenotaziones').as('postEntityRequest');
    cy.intercept('DELETE', '/api/stati-prenotaziones/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (statiPrenotazione) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/stati-prenotaziones/${statiPrenotazione.id}`,
      }).then(() => {
        statiPrenotazione = undefined;
      });
    }
  });

  it('StatiPrenotaziones menu should load StatiPrenotaziones page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('stati-prenotazione');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('StatiPrenotazione').should('exist');
    cy.url().should('match', statiPrenotazionePageUrlPattern);
  });

  describe('StatiPrenotazione page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(statiPrenotazionePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create StatiPrenotazione page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/stati-prenotazione/new$'));
        cy.getEntityCreateUpdateHeading('StatiPrenotazione');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', statiPrenotazionePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/stati-prenotaziones',
          body: statiPrenotazioneSample,
        }).then(({ body }) => {
          statiPrenotazione = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/stati-prenotaziones+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [statiPrenotazione],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(statiPrenotazionePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details StatiPrenotazione page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('statiPrenotazione');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', statiPrenotazionePageUrlPattern);
      });

      it('edit button click should load edit StatiPrenotazione page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('StatiPrenotazione');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', statiPrenotazionePageUrlPattern);
      });

      it('edit button click should load edit StatiPrenotazione page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('StatiPrenotazione');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', statiPrenotazionePageUrlPattern);
      });

      it('last delete button click should delete instance of StatiPrenotazione', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('statiPrenotazione').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', statiPrenotazionePageUrlPattern);

        statiPrenotazione = undefined;
      });
    });
  });

  describe('new StatiPrenotazione page', () => {
    beforeEach(() => {
      cy.visit(`${statiPrenotazionePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('StatiPrenotazione');
    });

    it('should create an instance of StatiPrenotazione', () => {
      cy.get(`[data-cy="descrizione"]`).type('own');
      cy.get(`[data-cy="descrizione"]`).should('have.value', 'own');

      cy.get(`[data-cy="codice"]`).select('WAITING');

      cy.get(`[data-cy="ordineAzione"]`).type('18605');
      cy.get(`[data-cy="ordineAzione"]`).should('have.value', '18605');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        statiPrenotazione = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', statiPrenotazionePageUrlPattern);
    });
  });
});
