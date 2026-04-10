import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IEventi, NewEventi } from '../eventi.model';

export type PartialUpdateEventi = Partial<IEventi> & Pick<IEventi, 'id'>;

export type EntityResponseType = HttpResponse<IEventi>;
export type EntityArrayResponseType = HttpResponse<IEventi[]>;

@Injectable({ providedIn: 'root' })
export class EventiService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/eventis');

  create(eventi: NewEventi): Observable<EntityResponseType> {
    return this.http.post<IEventi>(this.resourceUrl, eventi, { observe: 'response' });
  }

  update(eventi: IEventi): Observable<EntityResponseType> {
    return this.http.put<IEventi>(`${this.resourceUrl}/${this.getEventiIdentifier(eventi)}`, eventi, { observe: 'response' });
  }

  partialUpdate(eventi: PartialUpdateEventi): Observable<EntityResponseType> {
    return this.http.patch<IEventi>(`${this.resourceUrl}/${this.getEventiIdentifier(eventi)}`, eventi, { observe: 'response' });
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http.get<IEventi>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IEventi[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getEventiIdentifier(eventi: Pick<IEventi, 'id'>): string {
    return eventi.id;
  }

  compareEventi(o1: Pick<IEventi, 'id'> | null, o2: Pick<IEventi, 'id'> | null): boolean {
    return o1 && o2 ? this.getEventiIdentifier(o1) === this.getEventiIdentifier(o2) : o1 === o2;
  }

  getEventiPubblici(): Observable<IEventi[]> {
    return this.http.get<IEventi[]>(`${this.resourceUrl}/pubblici`);
  }

  addEventiToCollectionIfMissing<Type extends Pick<IEventi, 'id'>>(
    eventiCollection: Type[],
    ...eventisToCheck: (Type | null | undefined)[]
  ): Type[] {
    const eventis: Type[] = eventisToCheck.filter(isPresent);
    if (eventis.length > 0) {
      const eventiCollectionIdentifiers = eventiCollection.map(eventiItem => this.getEventiIdentifier(eventiItem));
      const eventisToAdd = eventis.filter(eventiItem => {
        const eventiIdentifier = this.getEventiIdentifier(eventiItem);
        if (eventiCollectionIdentifiers.includes(eventiIdentifier)) {
          return false;
        }
        eventiCollectionIdentifiers.push(eventiIdentifier);
        return true;
      });
      return [...eventisToAdd, ...eventiCollection];
    }
    return eventiCollection;
  }
}
