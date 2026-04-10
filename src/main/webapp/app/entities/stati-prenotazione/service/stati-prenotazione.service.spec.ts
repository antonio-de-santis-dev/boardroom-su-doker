import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IStatiPrenotazione } from '../stati-prenotazione.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../stati-prenotazione.test-samples';

import { StatiPrenotazioneService } from './stati-prenotazione.service';

const requireRestSample: IStatiPrenotazione = {
  ...sampleWithRequiredData,
};

describe('StatiPrenotazione Service', () => {
  let service: StatiPrenotazioneService;
  let httpMock: HttpTestingController;
  let expectedResult: IStatiPrenotazione | IStatiPrenotazione[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(StatiPrenotazioneService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a StatiPrenotazione', () => {
      const statiPrenotazione = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(statiPrenotazione).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a StatiPrenotazione', () => {
      const statiPrenotazione = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(statiPrenotazione).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a StatiPrenotazione', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of StatiPrenotazione', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a StatiPrenotazione', () => {
      const expected = true;

      service.delete('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addStatiPrenotazioneToCollectionIfMissing', () => {
      it('should add a StatiPrenotazione to an empty array', () => {
        const statiPrenotazione: IStatiPrenotazione = sampleWithRequiredData;
        expectedResult = service.addStatiPrenotazioneToCollectionIfMissing([], statiPrenotazione);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(statiPrenotazione);
      });

      it('should not add a StatiPrenotazione to an array that contains it', () => {
        const statiPrenotazione: IStatiPrenotazione = sampleWithRequiredData;
        const statiPrenotazioneCollection: IStatiPrenotazione[] = [
          {
            ...statiPrenotazione,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addStatiPrenotazioneToCollectionIfMissing(statiPrenotazioneCollection, statiPrenotazione);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a StatiPrenotazione to an array that doesn't contain it", () => {
        const statiPrenotazione: IStatiPrenotazione = sampleWithRequiredData;
        const statiPrenotazioneCollection: IStatiPrenotazione[] = [sampleWithPartialData];
        expectedResult = service.addStatiPrenotazioneToCollectionIfMissing(statiPrenotazioneCollection, statiPrenotazione);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(statiPrenotazione);
      });

      it('should add only unique StatiPrenotazione to an array', () => {
        const statiPrenotazioneArray: IStatiPrenotazione[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const statiPrenotazioneCollection: IStatiPrenotazione[] = [sampleWithRequiredData];
        expectedResult = service.addStatiPrenotazioneToCollectionIfMissing(statiPrenotazioneCollection, ...statiPrenotazioneArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const statiPrenotazione: IStatiPrenotazione = sampleWithRequiredData;
        const statiPrenotazione2: IStatiPrenotazione = sampleWithPartialData;
        expectedResult = service.addStatiPrenotazioneToCollectionIfMissing([], statiPrenotazione, statiPrenotazione2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(statiPrenotazione);
        expect(expectedResult).toContain(statiPrenotazione2);
      });

      it('should accept null and undefined values', () => {
        const statiPrenotazione: IStatiPrenotazione = sampleWithRequiredData;
        expectedResult = service.addStatiPrenotazioneToCollectionIfMissing([], null, statiPrenotazione, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(statiPrenotazione);
      });

      it('should return initial array if no StatiPrenotazione is added', () => {
        const statiPrenotazioneCollection: IStatiPrenotazione[] = [sampleWithRequiredData];
        expectedResult = service.addStatiPrenotazioneToCollectionIfMissing(statiPrenotazioneCollection, undefined, null);
        expect(expectedResult).toEqual(statiPrenotazioneCollection);
      });
    });

    describe('compareStatiPrenotazione', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareStatiPrenotazione(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
        const entity2 = null;

        const compareResult1 = service.compareStatiPrenotazione(entity1, entity2);
        const compareResult2 = service.compareStatiPrenotazione(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
        const entity2 = { id: 'c8406e2e-2acd-4853-b9fc-48d546a8bbaf' };

        const compareResult1 = service.compareStatiPrenotazione(entity1, entity2);
        const compareResult2 = service.compareStatiPrenotazione(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };
        const entity2 = { id: 'f1f87ac1-430c-4050-9372-fadd929ec890' };

        const compareResult1 = service.compareStatiPrenotazione(entity1, entity2);
        const compareResult2 = service.compareStatiPrenotazione(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
