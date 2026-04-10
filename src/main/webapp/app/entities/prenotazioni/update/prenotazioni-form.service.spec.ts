import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../prenotazioni.test-samples';

import { PrenotazioniFormService } from './prenotazioni-form.service';

describe('Prenotazioni Form Service', () => {
  let service: PrenotazioniFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrenotazioniFormService);
  });

  describe('Service methods', () => {
    describe('createPrenotazioniFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createPrenotazioniFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            data: expect.any(Object),
            oraInizio: expect.any(Object),
            oraFine: expect.any(Object),
            stato: expect.any(Object),
            utente: expect.any(Object),
            sala: expect.any(Object),
          }),
        );
      });

      it('passing IPrenotazioni should create a new form with FormGroup', () => {
        const formGroup = service.createPrenotazioniFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            data: expect.any(Object),
            oraInizio: expect.any(Object),
            oraFine: expect.any(Object),
            stato: expect.any(Object),
            utente: expect.any(Object),
            sala: expect.any(Object),
          }),
        );
      });
    });

    describe('getPrenotazioni', () => {
      it('should return NewPrenotazioni for default Prenotazioni initial value', () => {
        const formGroup = service.createPrenotazioniFormGroup(sampleWithNewData);

        const prenotazioni = service.getPrenotazioni(formGroup) as any;

        expect(prenotazioni).toMatchObject(sampleWithNewData);
      });

      it('should return NewPrenotazioni for empty Prenotazioni initial value', () => {
        const formGroup = service.createPrenotazioniFormGroup();

        const prenotazioni = service.getPrenotazioni(formGroup) as any;

        expect(prenotazioni).toMatchObject({});
      });

      it('should return IPrenotazioni', () => {
        const formGroup = service.createPrenotazioniFormGroup(sampleWithRequiredData);

        const prenotazioni = service.getPrenotazioni(formGroup) as any;

        expect(prenotazioni).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IPrenotazioni should not enable id FormControl', () => {
        const formGroup = service.createPrenotazioniFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewPrenotazioni should disable id FormControl', () => {
        const formGroup = service.createPrenotazioniFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
