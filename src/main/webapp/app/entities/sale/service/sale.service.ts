import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ISale, NewSale } from '../sale.model';

export type PartialUpdateSale = Partial<ISale> & Pick<ISale, 'id'>;

export type EntityResponseType = HttpResponse<ISale>;
export type EntityArrayResponseType = HttpResponse<ISale[]>;

@Injectable({ providedIn: 'root' })
export class SaleService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/sales');

  create(sale: NewSale): Observable<EntityResponseType> {
    return this.http.post<ISale>(this.resourceUrl, sale, { observe: 'response' });
  }

  update(sale: ISale): Observable<EntityResponseType> {
    return this.http.put<ISale>(`${this.resourceUrl}/${this.getSaleIdentifier(sale)}`, sale, { observe: 'response' });
  }

  partialUpdate(sale: PartialUpdateSale): Observable<EntityResponseType> {
    return this.http.patch<ISale>(`${this.resourceUrl}/${this.getSaleIdentifier(sale)}`, sale, { observe: 'response' });
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http.get<ISale>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ISale[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getSaleIdentifier(sale: Pick<ISale, 'id'>): string {
    return sale.id;
  }

  compareSale(o1: Pick<ISale, 'id'> | null, o2: Pick<ISale, 'id'> | null): boolean {
    return o1 && o2 ? this.getSaleIdentifier(o1) === this.getSaleIdentifier(o2) : o1 === o2;
  }

  addSaleToCollectionIfMissing<Type extends Pick<ISale, 'id'>>(
    saleCollection: Type[],
    ...salesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const sales: Type[] = salesToCheck.filter(isPresent);
    if (sales.length > 0) {
      const saleCollectionIdentifiers = saleCollection.map(saleItem => this.getSaleIdentifier(saleItem));
      const salesToAdd = sales.filter(saleItem => {
        const saleIdentifier = this.getSaleIdentifier(saleItem);
        if (saleCollectionIdentifiers.includes(saleIdentifier)) {
          return false;
        }
        saleCollectionIdentifiers.push(saleIdentifier);
        return true;
      });
      return [...salesToAdd, ...saleCollection];
    }
    return saleCollection;
  }
}
