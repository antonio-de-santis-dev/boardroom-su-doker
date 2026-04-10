import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { DATE_FORMAT } from 'app/config/input.constants';
import { IPrenotazioni } from '../prenotazioni.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../prenotazioni.test-samples';

import { PrenotazioniService, RestPrenotazioni } from './prenotazioni.service';

const requireRestSample: RestPrenotazioni = {
  ...sampleWithRequiredData,
  data: sampleWithRequiredData.data?.format(DATE_FORMAT),
};

describe('Prenotazioni Service', () => {
  let service: PrenotazioniService;
  let httpMock: HttpTestingController;
  let expectedResult: IPrenotazioni | IPrenotazioni[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(PrenotazioniService);
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

    it('should create a Prenotazioni', () => {
      const prenotazioni = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(prenotazioni).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Prenotazioni', () => {
      const prenotazioni = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(prenotazioni).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Prenotazioni', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Prenotazioni', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Prenotazioni', () => {
      const expected = true;

      service.delete('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addPrenotazioniToCollectionIfMissing', () => {
      it('should add a Prenotazioni to an empty array', () => {
        const prenotazioni: IPrenotazioni = sampleWithRequiredData;
        expectedResult = service.addPrenotazioniToCollectionIfMissing([], prenotazioni);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(prenotazioni);
      });

      it('should not add a Prenotazioni to an array that contains it', () => {
        const prenotazioni: IPrenotazioni = sampleWithRequiredData;
        const prenotazioniCollection: IPrenotazioni[] = [
          {
            ...prenotazioni,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addPrenotazioniToCollectionIfMissing(prenotazioniCollection, prenotazioni);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Prenotazioni to an array that doesn't contain it", () => {
        const prenotazioni: IPrenotazioni = sampleWithRequiredData;
        const prenotazioniCollection: IPrenotazioni[] = [sampleWithPartialData];
        expectedResult = service.addPrenotazioniToCollectionIfMissing(prenotazioniCollection, prenotazioni);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(prenotazioni);
      });

      it('should add only unique Prenotazioni to an array', () => {
        const prenotazioniArray: IPrenotazioni[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const prenotazioniCollection: IPrenotazioni[] = [sampleWithRequiredData];
        expectedResult = service.addPrenotazioniToCollectionIfMissing(prenotazioniCollection, ...prenotazioniArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const prenotazioni: IPrenotazioni = sampleWithRequiredData;
        const prenotazioni2: IPrenotazioni = sampleWithPartialData;
        expectedResult = service.addPrenotazioniToCollectionIfMissing([], prenotazioni, prenotazioni2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(prenotazioni);
        expect(expectedResult).toContain(prenotazioni2);
      });

      it('should accept null and undefined values', () => {
        const prenotazioni: IPrenotazioni = sampleWithRequiredData;
        expectedResult = service.addPrenotazioniToCollectionIfMissing([], null, prenotazioni, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(prenotazioni);
      });

      it('should return initial array if no Prenotazioni is added', () => {
        const prenotazioniCollection: IPrenotazioni[] = [sampleWithRequiredData];
        expectedResult = service.addPrenotazioniToCollectionIfMissing(prenotazioniCollection, undefined, null);
        expect(expectedResult).toEqual(prenotazioniCollection);
      });
    });

    describe('comparePrenotazioni', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.comparePrenotazioni(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
        const entity2 = null;

        const compareResult1 = service.comparePrenotazioni(entity1, entity2);
        const compareResult2 = service.comparePrenotazioni(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
        const entity2 = { id: '75278b19-0337-479a-8fc1-77c97e06f2cc' };

        const compareResult1 = service.comparePrenotazioni(entity1, entity2);
        const compareResult2 = service.comparePrenotazioni(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };
        const entity2 = { id: '51e023a7-13be-4a16-b1d0-ba867eeb1ec5' };

        const compareResult1 = service.comparePrenotazioni(entity1, entity2);
        const compareResult2 = service.comparePrenotazioni(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
