import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IEventi } from '../eventi.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../eventi.test-samples';

import { EventiService } from './eventi.service';

const requireRestSample: IEventi = {
  ...sampleWithRequiredData,
};

describe('Eventi Service', () => {
  let service: EventiService;
  let httpMock: HttpTestingController;
  let expectedResult: IEventi | IEventi[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(EventiService);
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

    it('should create a Eventi', () => {
      const eventi = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(eventi).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Eventi', () => {
      const eventi = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(eventi).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Eventi', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Eventi', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Eventi', () => {
      const expected = true;

      service.delete('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addEventiToCollectionIfMissing', () => {
      it('should add a Eventi to an empty array', () => {
        const eventi: IEventi = sampleWithRequiredData;
        expectedResult = service.addEventiToCollectionIfMissing([], eventi);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(eventi);
      });

      it('should not add a Eventi to an array that contains it', () => {
        const eventi: IEventi = sampleWithRequiredData;
        const eventiCollection: IEventi[] = [
          {
            ...eventi,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addEventiToCollectionIfMissing(eventiCollection, eventi);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Eventi to an array that doesn't contain it", () => {
        const eventi: IEventi = sampleWithRequiredData;
        const eventiCollection: IEventi[] = [sampleWithPartialData];
        expectedResult = service.addEventiToCollectionIfMissing(eventiCollection, eventi);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(eventi);
      });

      it('should add only unique Eventi to an array', () => {
        const eventiArray: IEventi[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const eventiCollection: IEventi[] = [sampleWithRequiredData];
        expectedResult = service.addEventiToCollectionIfMissing(eventiCollection, ...eventiArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const eventi: IEventi = sampleWithRequiredData;
        const eventi2: IEventi = sampleWithPartialData;
        expectedResult = service.addEventiToCollectionIfMissing([], eventi, eventi2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(eventi);
        expect(expectedResult).toContain(eventi2);
      });

      it('should accept null and undefined values', () => {
        const eventi: IEventi = sampleWithRequiredData;
        expectedResult = service.addEventiToCollectionIfMissing([], null, eventi, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(eventi);
      });

      it('should return initial array if no Eventi is added', () => {
        const eventiCollection: IEventi[] = [sampleWithRequiredData];
        expectedResult = service.addEventiToCollectionIfMissing(eventiCollection, undefined, null);
        expect(expectedResult).toEqual(eventiCollection);
      });
    });

    describe('compareEventi', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareEventi(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' };
        const entity2 = null;

        const compareResult1 = service.compareEventi(entity1, entity2);
        const compareResult2 = service.compareEventi(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' };
        const entity2 = { id: '3a68d63d-969f-433b-90b6-08f04124e5be' };

        const compareResult1 = service.compareEventi(entity1, entity2);
        const compareResult2 = service.compareEventi(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' };
        const entity2 = { id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' };

        const compareResult1 = service.compareEventi(entity1, entity2);
        const compareResult2 = service.compareEventi(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
