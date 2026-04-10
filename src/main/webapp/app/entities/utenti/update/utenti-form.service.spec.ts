import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../utenti.test-samples';

import { UtentiFormService } from './utenti-form.service';

describe('Utenti Form Service', () => {
  let service: UtentiFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UtentiFormService);
  });

  describe('Service methods', () => {
    describe('createUtentiFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createUtentiFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            nome: expect.any(Object),
            nomeAzienda: expect.any(Object),
            numeroDiTelefono: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing IUtenti should create a new form with FormGroup', () => {
        const formGroup = service.createUtentiFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            nome: expect.any(Object),
            nomeAzienda: expect.any(Object),
            numeroDiTelefono: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getUtenti', () => {
      it('should return NewUtenti for default Utenti initial value', () => {
        const formGroup = service.createUtentiFormGroup(sampleWithNewData);

        const utenti = service.getUtenti(formGroup) as any;

        expect(utenti).toMatchObject(sampleWithNewData);
      });

      it('should return NewUtenti for empty Utenti initial value', () => {
        const formGroup = service.createUtentiFormGroup();

        const utenti = service.getUtenti(formGroup) as any;

        expect(utenti).toMatchObject({});
      });

      it('should return IUtenti', () => {
        const formGroup = service.createUtentiFormGroup(sampleWithRequiredData);

        const utenti = service.getUtenti(formGroup) as any;

        expect(utenti).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IUtenti should not enable id FormControl', () => {
        const formGroup = service.createUtentiFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewUtenti should disable id FormControl', () => {
        const formGroup = service.createUtentiFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
