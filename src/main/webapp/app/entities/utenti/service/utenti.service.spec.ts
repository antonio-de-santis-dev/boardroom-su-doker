import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IUtenti } from '../utenti.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../utenti.test-samples';

import { UtentiService } from './utenti.service';

const requireRestSample: IUtenti = {
  ...sampleWithRequiredData,
};

describe('Utenti Service', () => {
  let service: UtentiService;
  let httpMock: HttpTestingController;
  let expectedResult: IUtenti | IUtenti[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(UtentiService);
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

    it('should create a Utenti', () => {
      const utenti = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(utenti).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Utenti', () => {
      const utenti = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(utenti).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Utenti', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Utenti', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Utenti', () => {
      const expected = true;

      service.delete('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addUtentiToCollectionIfMissing', () => {
      it('should add a Utenti to an empty array', () => {
        const utenti: IUtenti = sampleWithRequiredData;
        expectedResult = service.addUtentiToCollectionIfMissing([], utenti);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(utenti);
      });

      it('should not add a Utenti to an array that contains it', () => {
        const utenti: IUtenti = sampleWithRequiredData;
        const utentiCollection: IUtenti[] = [
          {
            ...utenti,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addUtentiToCollectionIfMissing(utentiCollection, utenti);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Utenti to an array that doesn't contain it", () => {
        const utenti: IUtenti = sampleWithRequiredData;
        const utentiCollection: IUtenti[] = [sampleWithPartialData];
        expectedResult = service.addUtentiToCollectionIfMissing(utentiCollection, utenti);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(utenti);
      });

      it('should add only unique Utenti to an array', () => {
        const utentiArray: IUtenti[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const utentiCollection: IUtenti[] = [sampleWithRequiredData];
        expectedResult = service.addUtentiToCollectionIfMissing(utentiCollection, ...utentiArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const utenti: IUtenti = sampleWithRequiredData;
        const utenti2: IUtenti = sampleWithPartialData;
        expectedResult = service.addUtentiToCollectionIfMissing([], utenti, utenti2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(utenti);
        expect(expectedResult).toContain(utenti2);
      });

      it('should accept null and undefined values', () => {
        const utenti: IUtenti = sampleWithRequiredData;
        expectedResult = service.addUtentiToCollectionIfMissing([], null, utenti, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(utenti);
      });

      it('should return initial array if no Utenti is added', () => {
        const utentiCollection: IUtenti[] = [sampleWithRequiredData];
        expectedResult = service.addUtentiToCollectionIfMissing(utentiCollection, undefined, null);
        expect(expectedResult).toEqual(utentiCollection);
      });
    });

    describe('compareUtenti', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareUtenti(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
        const entity2 = null;

        const compareResult1 = service.compareUtenti(entity1, entity2);
        const compareResult2 = service.compareUtenti(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
        const entity2 = { id: '58e889ee-0365-4883-a3bf-e668883718c9' };

        const compareResult1 = service.compareUtenti(entity1, entity2);
        const compareResult2 = service.compareUtenti(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
        const entity2 = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };

        const compareResult1 = service.compareUtenti(entity1, entity2);
        const compareResult2 = service.compareUtenti(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
