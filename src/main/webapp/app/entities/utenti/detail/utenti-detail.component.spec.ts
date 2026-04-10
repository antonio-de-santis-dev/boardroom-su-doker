import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { UtentiDetailComponent } from './utenti-detail.component';

describe('Utenti Management Detail Component', () => {
  let comp: UtentiDetailComponent;
  let fixture: ComponentFixture<UtentiDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UtentiDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./utenti-detail.component').then(m => m.UtentiDetailComponent),
              resolve: { utenti: () => of({ id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(UtentiDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UtentiDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load utenti on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', UtentiDetailComponent);

      // THEN
      expect(instance.utenti()).toEqual(expect.objectContaining({ id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
