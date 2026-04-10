import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { StatiPrenotazioneDetailComponent } from './stati-prenotazione-detail.component';

describe('StatiPrenotazione Management Detail Component', () => {
  let comp: StatiPrenotazioneDetailComponent;
  let fixture: ComponentFixture<StatiPrenotazioneDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatiPrenotazioneDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./stati-prenotazione-detail.component').then(m => m.StatiPrenotazioneDetailComponent),
              resolve: { statiPrenotazione: () => of({ id: 'f1f87ac1-430c-4050-9372-fadd929ec890' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(StatiPrenotazioneDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StatiPrenotazioneDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load statiPrenotazione on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', StatiPrenotazioneDetailComponent);

      // THEN
      expect(instance.statiPrenotazione()).toEqual(expect.objectContaining({ id: 'f1f87ac1-430c-4050-9372-fadd929ec890' }));
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
