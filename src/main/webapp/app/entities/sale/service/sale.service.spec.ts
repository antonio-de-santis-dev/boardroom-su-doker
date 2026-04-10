import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ISale } from '../sale.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../sale.test-samples';

import { SaleService } from './sale.service';

const requireRestSample: ISale = {
  ...sampleWithRequiredData,
};

describe('Sale Service', () => {
  let service: SaleService;
  let httpMock: HttpTestingController;
  let expectedResult: ISale | ISale[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(SaleService);
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

    it('should create a Sale', () => {
      const sale = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(sale).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Sale', () => {
      const sale = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(sale).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Sale', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Sale', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Sale', () => {
      const expected = true;

      service.delete('9fec3727-3421-4967-b213-ba36557ca194').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addSaleToCollectionIfMissing', () => {
      it('should add a Sale to an empty array', () => {
        const sale: ISale = sampleWithRequiredData;
        expectedResult = service.addSaleToCollectionIfMissing([], sale);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(sale);
      });

      it('should not add a Sale to an array that contains it', () => {
        const sale: ISale = sampleWithRequiredData;
        const saleCollection: ISale[] = [
          {
            ...sale,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addSaleToCollectionIfMissing(saleCollection, sale);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Sale to an array that doesn't contain it", () => {
        const sale: ISale = sampleWithRequiredData;
        const saleCollection: ISale[] = [sampleWithPartialData];
        expectedResult = service.addSaleToCollectionIfMissing(saleCollection, sale);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(sale);
      });

      it('should add only unique Sale to an array', () => {
        const saleArray: ISale[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const saleCollection: ISale[] = [sampleWithRequiredData];
        expectedResult = service.addSaleToCollectionIfMissing(saleCollection, ...saleArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const sale: ISale = sampleWithRequiredData;
        const sale2: ISale = sampleWithPartialData;
        expectedResult = service.addSaleToCollectionIfMissing([], sale, sale2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(sale);
        expect(expectedResult).toContain(sale2);
      });

      it('should accept null and undefined values', () => {
        const sale: ISale = sampleWithRequiredData;
        expectedResult = service.addSaleToCollectionIfMissing([], null, sale, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(sale);
      });

      it('should return initial array if no Sale is added', () => {
        const saleCollection: ISale[] = [sampleWithRequiredData];
        expectedResult = service.addSaleToCollectionIfMissing(saleCollection, undefined, null);
        expect(expectedResult).toEqual(saleCollection);
      });
    });

    describe('compareSale', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareSale(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' };
        const entity2 = null;

        const compareResult1 = service.compareSale(entity1, entity2);
        const compareResult2 = service.compareSale(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' };
        const entity2 = { id: '40c7b91e-348d-42f3-85b7-68229c210645' };

        const compareResult1 = service.compareSale(entity1, entity2);
        const compareResult2 = service.compareSale(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' };
        const entity2 = { id: '157c8880-42a9-4d24-b13f-c77ce41f45a7' };

        const compareResult1 = service.compareSale(entity1, entity2);
        const compareResult2 = service.compareSale(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
