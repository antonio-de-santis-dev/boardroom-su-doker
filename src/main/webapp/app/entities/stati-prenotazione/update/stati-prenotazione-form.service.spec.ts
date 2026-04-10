import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../stati-prenotazione.test-samples';

import { StatiPrenotazioneFormService } from './stati-prenotazione-form.service';

describe('StatiPrenotazione Form Service', () => {
  let service: StatiPrenotazioneFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StatiPrenotazioneFormService);
  });

  describe('Service methods', () => {
    describe('createStatiPrenotazioneFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createStatiPrenotazioneFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            descrizione: expect.any(Object),
            codice: expect.any(Object),
            ordineAzione: expect.any(Object),
          }),
        );
      });

      it('passing IStatiPrenotazione should create a new form with FormGroup', () => {
        const formGroup = service.createStatiPrenotazioneFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            descrizione: expect.any(Object),
            codice: expect.any(Object),
            ordineAzione: expect.any(Object),
          }),
        );
      });
    });

    describe('getStatiPrenotazione', () => {
      it('should return NewStatiPrenotazione for default StatiPrenotazione initial value', () => {
        const formGroup = service.createStatiPrenotazioneFormGroup(sampleWithNewData);

        const statiPrenotazione = service.getStatiPrenotazione(formGroup) as any;

        expect(statiPrenotazione).toMatchObject(sampleWithNewData);
      });

      it('should return NewStatiPrenotazione for empty StatiPrenotazione initial value', () => {
        const formGroup = service.createStatiPrenotazioneFormGroup();

        const statiPrenotazione = service.getStatiPrenotazione(formGroup) as any;

        expect(statiPrenotazione).toMatchObject({});
      });

      it('should return IStatiPrenotazione', () => {
        const formGroup = service.createStatiPrenotazioneFormGroup(sampleWithRequiredData);

        const statiPrenotazione = service.getStatiPrenotazione(formGroup) as any;

        expect(statiPrenotazione).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IStatiPrenotazione should not enable id FormControl', () => {
        const formGroup = service.createStatiPrenotazioneFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewStatiPrenotazione should disable id FormControl', () => {
        const formGroup = service.createStatiPrenotazioneFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
