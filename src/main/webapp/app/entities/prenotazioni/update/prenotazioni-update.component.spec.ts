import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IStatiPrenotazione } from 'app/entities/stati-prenotazione/stati-prenotazione.model';
import { StatiPrenotazioneService } from 'app/entities/stati-prenotazione/service/stati-prenotazione.service';
import { IUtenti } from 'app/entities/utenti/utenti.model';
import { UtentiService } from 'app/entities/utenti/service/utenti.service';
import { ISale } from 'app/entities/sale/sale.model';
import { SaleService } from 'app/entities/sale/service/sale.service';
import { IPrenotazioni } from '../prenotazioni.model';
import { PrenotazioniService } from '../service/prenotazioni.service';
import { PrenotazioniFormService } from './prenotazioni-form.service';

import { PrenotazioniUpdateComponent } from './prenotazioni-update.component';

describe('Prenotazioni Management Update Component', () => {
  let comp: PrenotazioniUpdateComponent;
  let fixture: ComponentFixture<PrenotazioniUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let prenotazioniFormService: PrenotazioniFormService;
  let prenotazioniService: PrenotazioniService;
  let statiPrenotazioneService: StatiPrenotazioneService;
  let utentiService: UtentiService;
  let saleService: SaleService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PrenotazioniUpdateComponent],
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
      .overrideTemplate(PrenotazioniUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PrenotazioniUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    prenotazioniFormService = TestBed.inject(PrenotazioniFormService);
    prenotazioniService = TestBed.inject(PrenotazioniService);
    statiPrenotazioneService = TestBed.inject(StatiPrenotazioneService);
    utentiService = TestBed.inject(UtentiService);
    saleService = TestBed.inject(SaleService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call StatiPrenotazione query and add missing value', () => {
      const prenotazioni: IPrenotazioni = { id: '75278b19-0337-479a-8fc1-77c97e06f2cc' };
      const stato: IStatiPrenotazione = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
      prenotazioni.stato = stato;

      const statiPrenotazioneCollection: IStatiPrenotazione[] = [{ id: 'f1f87ac1-430c-4050-9372-fadd929ec890' }];
      jest.spyOn(statiPrenotazioneService, 'query').mockReturnValue(of(new HttpResponse({ body: statiPrenotazioneCollection })));
      const additionalStatiPrenotaziones = [stato];
      const expectedCollection: IStatiPrenotazione[] = [...additionalStatiPrenotaziones, ...statiPrenotazioneCollection];
      jest.spyOn(statiPrenotazioneService, 'addStatiPrenotazioneToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ prenotazioni });
      comp.ngOnInit();

      expect(statiPrenotazioneService.query).toHaveBeenCalled();
      expect(statiPrenotazioneService.addStatiPrenotazioneToCollectionIfMissing).toHaveBeenCalledWith(
        statiPrenotazioneCollection,
        ...additionalStatiPrenotaziones.map(expect.objectContaining),
      );
      expect(comp.statiPrenotazionesSharedCollection).toEqual(expectedCollection);
    });

    it('should call Utenti query and add missing value', () => {
      const prenotazioni: IPrenotazioni = { id: '75278b19-0337-479a-8fc1-77c97e06f2cc' };
      const utente: IUtenti = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
      prenotazioni.utente = utente;

      const utentiCollection: IUtenti[] = [{ id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' }];
      jest.spyOn(utentiService, 'query').mockReturnValue(of(new HttpResponse({ body: utentiCollection })));
      const additionalUtentis = [utente];
      const expectedCollection: IUtenti[] = [...additionalUtentis, ...utentiCollection];
      jest.spyOn(utentiService, 'addUtentiToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ prenotazioni });
      comp.ngOnInit();

      expect(utentiService.query).toHaveBeenCalled();
      expect(utentiService.addUtentiToCollectionIfMissing).toHaveBeenCalledWith(
        utentiCollection,
        ...additionalUtentis.map(expect.objectContaining),
      );
      expect(comp.utentisSharedCollection).toEqual(expectedCollection);
    });

    it('should call Sale query and add missing value', () => {
      const prenotazioni: IPrenotazioni = { id: '75278b19-0337-479a-8fc1-77c97e06f2cc' };
      const sala: ISale = { id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' };
      prenotazioni.sala = sala;

      const saleCollection: ISale[] = [{ id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' }];
      jest.spyOn(saleService, 'query').mockReturnValue(of(new HttpResponse({ body: saleCollection })));
      const additionalSales = [sala];
      const expectedCollection: ISale[] = [...additionalSales, ...saleCollection];
      jest.spyOn(saleService, 'addSaleToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ prenotazioni });
      comp.ngOnInit();

      expect(saleService.query).toHaveBeenCalled();
      expect(saleService.addSaleToCollectionIfMissing).toHaveBeenCalledWith(
        saleCollection,
        ...additionalSales.map(expect.objectContaining),
      );
      expect(comp.salesSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const prenotazioni: IPrenotazioni = { id: '75278b19-0337-479a-8fc1-77c97e06f2cc' };
      const stato: IStatiPrenotazione = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
      prenotazioni.stato = stato;
      const utente: IUtenti = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
      prenotazioni.utente = utente;
      const sala: ISale = { id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' };
      prenotazioni.sala = sala;

      activatedRoute.data = of({ prenotazioni });
      comp.ngOnInit();

      expect(comp.statiPrenotazionesSharedCollection).toContainEqual(stato);
      expect(comp.utentisSharedCollection).toContainEqual(utente);
      expect(comp.salesSharedCollection).toContainEqual(sala);
      expect(comp.prenotazioni).toEqual(prenotazioni);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPrenotazioni>>();
      const prenotazioni = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
      jest.spyOn(prenotazioniFormService, 'getPrenotazioni').mockReturnValue(prenotazioni);
      jest.spyOn(prenotazioniService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ prenotazioni });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: prenotazioni }));
      saveSubject.complete();

      // THEN
      expect(prenotazioniFormService.getPrenotazioni).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(prenotazioniService.update).toHaveBeenCalledWith(expect.objectContaining(prenotazioni));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPrenotazioni>>();
      const prenotazioni = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
      jest.spyOn(prenotazioniFormService, 'getPrenotazioni').mockReturnValue({ id: null });
      jest.spyOn(prenotazioniService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ prenotazioni: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: prenotazioni }));
      saveSubject.complete();

      // THEN
      expect(prenotazioniFormService.getPrenotazioni).toHaveBeenCalled();
      expect(prenotazioniService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IPrenotazioni>>();
      const prenotazioni = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
      jest.spyOn(prenotazioniService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ prenotazioni });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(prenotazioniService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareStatiPrenotazione', () => {
      it('should forward to statiPrenotazioneService', () => {
        const entity = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
        const entity2 = { id: 'c8406e2e-2acd-4853-b9fc-48d546a8bbaf' };
        jest.spyOn(statiPrenotazioneService, 'compareStatiPrenotazione');
        comp.compareStatiPrenotazione(entity, entity2);
        expect(statiPrenotazioneService.compareStatiPrenotazione).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareUtenti', () => {
      it('should forward to utentiService', () => {
        const entity = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
        const entity2 = { id: '58e889ee-0365-4883-a3bf-e668883718c9' };
        jest.spyOn(utentiService, 'compareUtenti');
        comp.compareUtenti(entity, entity2);
        expect(utentiService.compareUtenti).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareSale', () => {
      it('should forward to saleService', () => {
        const entity = { id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' };
        const entity2 = { id: '40c7b91e-348d-42f3-85b7-68229c210645' };
        jest.spyOn(saleService, 'compareSale');
        comp.compareSale(entity, entity2);
        expect(saleService.compareSale).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
