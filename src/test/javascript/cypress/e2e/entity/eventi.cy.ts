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

describe('Eventi e2e test', () => {
  const eventiPageUrl = '/eventi';
  const eventiPageUrlPattern = new RegExp('/eventi(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const eventiSample = { titolo: 'hm even', tipo: 'PRIVATO' };

  let eventi;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/eventis+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/eventis').as('postEntityRequest');
    cy.intercept('DELETE', '/api/eventis/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (eventi) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/eventis/${eventi.id}`,
      }).then(() => {
        eventi = undefined;
      });
    }
  });

  it('Eventis menu should load Eventis page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('eventi');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Eventi').should('exist');
    cy.url().should('match', eventiPageUrlPattern);
  });

  describe('Eventi page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(eventiPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Eventi page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/eventi/new$'));
        cy.getEntityCreateUpdateHeading('Eventi');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', eventiPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/eventis',
          body: eventiSample,
        }).then(({ body }) => {
          eventi = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/eventis+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/eventis?page=0&size=20>; rel="last",<http://localhost/api/eventis?page=0&size=20>; rel="first"',
              },
              body: [eventi],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(eventiPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Eventi page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('eventi');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', eventiPageUrlPattern);
      });

      it('edit button click should load edit Eventi page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Eventi');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', eventiPageUrlPattern);
      });

      it('edit button click should load edit Eventi page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Eventi');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', eventiPageUrlPattern);
      });

      it('last delete button click should delete instance of Eventi', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('eventi').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', eventiPageUrlPattern);

        eventi = undefined;
      });
    });
  });

  describe('new Eventi page', () => {
    beforeEach(() => {
      cy.visit(`${eventiPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Eventi');
    });

    it('should create an instance of Eventi', () => {
      cy.get(`[data-cy="titolo"]`).type('but while');
      cy.get(`[data-cy="titolo"]`).should('have.value', 'but while');

      cy.get(`[data-cy="tipo"]`).select('PUBBLICO');

      cy.get(`[data-cy="prezzo"]`).type('18410.55');
      cy.get(`[data-cy="prezzo"]`).should('have.value', '18410.55');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        eventi = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', eventiPageUrlPattern);
    });
  });
});
