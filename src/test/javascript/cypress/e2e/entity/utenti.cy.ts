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

describe('Utenti e2e test', () => {
  const utentiPageUrl = '/utenti';
  const utentiPageUrlPattern = new RegExp('/utenti(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const utentiSample = { nome: 'beneficial chapel woot', numeroDiTelefono: 'loosely' };

  let utenti;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/utentis+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/utentis').as('postEntityRequest');
    cy.intercept('DELETE', '/api/utentis/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (utenti) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/utentis/${utenti.id}`,
      }).then(() => {
        utenti = undefined;
      });
    }
  });

  it('Utentis menu should load Utentis page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('utenti');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Utenti').should('exist');
    cy.url().should('match', utentiPageUrlPattern);
  });

  describe('Utenti page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(utentiPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Utenti page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/utenti/new$'));
        cy.getEntityCreateUpdateHeading('Utenti');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', utentiPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/utentis',
          body: utentiSample,
        }).then(({ body }) => {
          utenti = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/utentis+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [utenti],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(utentiPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Utenti page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('utenti');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', utentiPageUrlPattern);
      });

      it('edit button click should load edit Utenti page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Utenti');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', utentiPageUrlPattern);
      });

      it('edit button click should load edit Utenti page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Utenti');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', utentiPageUrlPattern);
      });

      it('last delete button click should delete instance of Utenti', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('utenti').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', utentiPageUrlPattern);

        utenti = undefined;
      });
    });
  });

  describe('new Utenti page', () => {
    beforeEach(() => {
      cy.visit(`${utentiPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Utenti');
    });

    it('should create an instance of Utenti', () => {
      cy.get(`[data-cy="nome"]`).type('gosh failing');
      cy.get(`[data-cy="nome"]`).should('have.value', 'gosh failing');

      cy.get(`[data-cy="nomeAzienda"]`).type('wicked expostulate heartache');
      cy.get(`[data-cy="nomeAzienda"]`).should('have.value', 'wicked expostulate heartache');

      cy.get(`[data-cy="numeroDiTelefono"]`).type('bah duh event');
      cy.get(`[data-cy="numeroDiTelefono"]`).should('have.value', 'bah duh event');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        utenti = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', utentiPageUrlPattern);
    });
  });
});
