import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { StatiPrenotazioneService } from '../service/stati-prenotazione.service';
import { IStatiPrenotazione } from '../stati-prenotazione.model';
import { StatiPrenotazioneFormService } from './stati-prenotazione-form.service';

import { StatiPrenotazioneUpdateComponent } from './stati-prenotazione-update.component';

describe('StatiPrenotazione Management Update Component', () => {
  let comp: StatiPrenotazioneUpdateComponent;
  let fixture: ComponentFixture<StatiPrenotazioneUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let statiPrenotazioneFormService: StatiPrenotazioneFormService;
  let statiPrenotazioneService: StatiPrenotazioneService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StatiPrenotazioneUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(StatiPrenotazioneUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(StatiPrenotazioneUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    statiPrenotazioneFormService = TestBed.inject(StatiPrenotazioneFormService);
    statiPrenotazioneService = TestBed.inject(StatiPrenotazioneService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const statiPrenotazione: IStatiPrenotazione = { id: 'c8406e2e-2acd-4853-b9fc-48d546a8bbaf' };

      activatedRoute.data = of({ statiPrenotazione });
      comp.ngOnInit();

      expect(comp.statiPrenotazione).toEqual(statiPrenotazione);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IStatiPrenotazione>>();
      const statiPrenotazione = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
      jest.spyOn(statiPrenotazioneFormService, 'getStatiPrenotazione').mockReturnValue(statiPrenotazione);
      jest.spyOn(statiPrenotazioneService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ statiPrenotazione });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: statiPrenotazione }));
      saveSubject.complete();

      // THEN
      expect(statiPrenotazioneFormService.getStatiPrenotazione).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(statiPrenotazioneService.update).toHaveBeenCalledWith(expect.objectContaining(statiPrenotazione));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IStatiPrenotazione>>();
      const statiPrenotazione = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
      jest.spyOn(statiPrenotazioneFormService, 'getStatiPrenotazione').mockReturnValue({ id: null });
      jest.spyOn(statiPrenotazioneService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ statiPrenotazione: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: statiPrenotazione }));
      saveSubject.complete();

      // THEN
      expect(statiPrenotazioneFormService.getStatiPrenotazione).toHaveBeenCalled();
      expect(statiPrenotazioneService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IStatiPrenotazione>>();
      const statiPrenotazione = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
      jest.spyOn(statiPrenotazioneService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ statiPrenotazione });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(statiPrenotazioneService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
