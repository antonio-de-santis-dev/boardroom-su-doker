import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../eventi.test-samples';

import { EventiFormService } from './eventi-form.service';

describe('Eventi Form Service', () => {
  let service: EventiFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EventiFormService);
  });

  describe('Service methods', () => {
    describe('createEventiFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createEventiFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            titolo: expect.any(Object),
            tipo: expect.any(Object),
            prezzo: expect.any(Object),
            prenotazione: expect.any(Object),
          }),
        );
      });

      it('passing IEventi should create a new form with FormGroup', () => {
        const formGroup = service.createEventiFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            titolo: expect.any(Object),
            tipo: expect.any(Object),
            prezzo: expect.any(Object),
            prenotazione: expect.any(Object),
          }),
        );
      });
    });

    describe('getEventi', () => {
      it('should return NewEventi for default Eventi initial value', () => {
        const formGroup = service.createEventiFormGroup(sampleWithNewData);

        const eventi = service.getEventi(formGroup) as any;

        expect(eventi).toMatchObject(sampleWithNewData);
      });

      it('should return NewEventi for empty Eventi initial value', () => {
        const formGroup = service.createEventiFormGroup();

        const eventi = service.getEventi(formGroup) as any;

        expect(eventi).toMatchObject({});
      });

      it('should return IEventi', () => {
        const formGroup = service.createEventiFormGroup(sampleWithRequiredData);

        const eventi = service.getEventi(formGroup) as any;

        expect(eventi).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IEventi should not enable id FormControl', () => {
        const formGroup = service.createEventiFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewEventi should disable id FormControl', () => {
        const formGroup = service.createEventiFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
