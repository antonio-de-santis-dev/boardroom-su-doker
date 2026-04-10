import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IPrenotazioni } from 'app/entities/prenotazioni/prenotazioni.model';
import { PrenotazioniService } from 'app/entities/prenotazioni/service/prenotazioni.service';
import { EventiService } from '../service/eventi.service';
import { IEventi } from '../eventi.model';
import { EventiFormService } from './eventi-form.service';

import { EventiUpdateComponent } from './eventi-update.component';

describe('Eventi Management Update Component', () => {
  let comp: EventiUpdateComponent;
  let fixture: ComponentFixture<EventiUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let eventiFormService: EventiFormService;
  let eventiService: EventiService;
  let prenotazioniService: PrenotazioniService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EventiUpdateComponent],
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
      .overrideTemplate(EventiUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(EventiUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    eventiFormService = TestBed.inject(EventiFormService);
    eventiService = TestBed.inject(EventiService);
    prenotazioniService = TestBed.inject(PrenotazioniService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Prenotazioni query and add missing value', () => {
      const eventi: IEventi = { id: '3a68d63d-969f-433b-90b6-08f04124e5be' };
      const prenotazione: IPrenotazioni = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
      eventi.prenotazione = prenotazione;

      const prenotazioniCollection: IPrenotazioni[] = [{ id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' }];
      jest.spyOn(prenotazioniService, 'query').mockReturnValue(of(new HttpResponse({ body: prenotazioniCollection })));
      const additionalPrenotazionis = [prenotazione];
      const expectedCollection: IPrenotazioni[] = [...additionalPrenotazionis, ...prenotazioniCollection];
      jest.spyOn(prenotazioniService, 'addPrenotazioniToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ eventi });
      comp.ngOnInit();

      expect(prenotazioniService.query).toHaveBeenCalled();
      expect(prenotazioniService.addPrenotazioniToCollectionIfMissing).toHaveBeenCalledWith(
        prenotazioniCollection,
        ...additionalPrenotazionis.map(expect.objectContaining),
      );
      expect(comp.prenotazionisSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const eventi: IEventi = { id: '3a68d63d-969f-433b-90b6-08f04124e5be' };
      const prenotazione: IPrenotazioni = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
      eventi.prenotazione = prenotazione;

      activatedRoute.data = of({ eventi });
      comp.ngOnInit();

      expect(comp.prenotazionisSharedCollection).toContainEqual(prenotazione);
      expect(comp.eventi).toEqual(eventi);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IEventi>>();
      const eventi = { id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' };
      jest.spyOn(eventiFormService, 'getEventi').mockReturnValue(eventi);
      jest.spyOn(eventiService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ eventi });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: eventi }));
      saveSubject.complete();

      // THEN
      expect(eventiFormService.getEventi).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(eventiService.update).toHaveBeenCalledWith(expect.objectContaining(eventi));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IEventi>>();
      const eventi = { id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' };
      jest.spyOn(eventiFormService, 'getEventi').mockReturnValue({ id: null });
      jest.spyOn(eventiService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ eventi: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: eventi }));
      saveSubject.complete();

      // THEN
      expect(eventiFormService.getEventi).toHaveBeenCalled();
      expect(eventiService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IEventi>>();
      const eventi = { id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' };
      jest.spyOn(eventiService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ eventi });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(eventiService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('comparePrenotazioni', () => {
      it('should forward to prenotazioniService', () => {
        const entity = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
        const entity2 = { id: '75278b19-0337-479a-8fc1-77c97e06f2cc' };
        jest.spyOn(prenotazioniService, 'comparePrenotazioni');
        comp.comparePrenotazioni(entity, entity2);
        expect(prenotazioniService.comparePrenotazioni).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
