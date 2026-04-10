import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IUtenti, NewUtenti } from '../utenti.model';

export type PartialUpdateUtenti = Partial<IUtenti> & Pick<IUtenti, 'id'>;

export type EntityResponseType = HttpResponse<IUtenti>;
export type EntityArrayResponseType = HttpResponse<IUtenti[]>;

@Injectable({ providedIn: 'root' })
export class UtentiService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/utentis');

  create(utenti: NewUtenti): Observable<EntityResponseType> {
    return this.http.post<IUtenti>(this.resourceUrl, utenti, { observe: 'response' });
  }

  update(utenti: IUtenti): Observable<EntityResponseType> {
    return this.http.put<IUtenti>(`${this.resourceUrl}/${this.getUtentiIdentifier(utenti)}`, utenti, { observe: 'response' });
  }

  partialUpdate(utenti: PartialUpdateUtenti): Observable<EntityResponseType> {
    return this.http.patch<IUtenti>(`${this.resourceUrl}/${this.getUtentiIdentifier(utenti)}`, utenti, { observe: 'response' });
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http.get<IUtenti>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IUtenti[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getUtentiIdentifier(utenti: Pick<IUtenti, 'id'>): string {
    return utenti.id;
  }

  compareUtenti(o1: Pick<IUtenti, 'id'> | null, o2: Pick<IUtenti, 'id'> | null): boolean {
    return o1 && o2 ? this.getUtentiIdentifier(o1) === this.getUtentiIdentifier(o2) : o1 === o2;
  }

  addUtentiToCollectionIfMissing<Type extends Pick<IUtenti, 'id'>>(
    utentiCollection: Type[],
    ...utentisToCheck: (Type | null | undefined)[]
  ): Type[] {
    const utentis: Type[] = utentisToCheck.filter(isPresent);
    if (utentis.length > 0) {
      const utentiCollectionIdentifiers = utentiCollection.map(utentiItem => this.getUtentiIdentifier(utentiItem));
      const utentisToAdd = utentis.filter(utentiItem => {
        const utentiIdentifier = this.getUtentiIdentifier(utentiItem);
        if (utentiCollectionIdentifiers.includes(utentiIdentifier)) {
          return false;
        }
        utentiCollectionIdentifiers.push(utentiIdentifier);
        return true;
      });
      return [...utentisToAdd, ...utentiCollection];
    }
    return utentiCollection;
  }
}
