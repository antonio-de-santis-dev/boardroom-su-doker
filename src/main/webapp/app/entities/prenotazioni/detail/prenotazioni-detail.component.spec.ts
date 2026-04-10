import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { PrenotazioniDetailComponent } from './prenotazioni-detail.component';

describe('Prenotazioni Management Detail Component', () => {
  let comp: PrenotazioniDetailComponent;
  let fixture: ComponentFixture<PrenotazioniDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrenotazioniDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./prenotazioni-detail.component').then(m => m.PrenotazioniDetailComponent),
              resolve: { prenotazioni: () => of({ id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(PrenotazioniDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PrenotazioniDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load prenotazioni on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', PrenotazioniDetailComponent);

      // THEN
      expect(instance.prenotazioni()).toEqual(expect.objectContaining({ id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' }));
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
