import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IStatiPrenotazione, NewStatiPrenotazione } from '../stati-prenotazione.model';

export type PartialUpdateStatiPrenotazione = Partial<IStatiPrenotazione> & Pick<IStatiPrenotazione, 'id'>;

export type EntityResponseType = HttpResponse<IStatiPrenotazione>;
export type EntityArrayResponseType = HttpResponse<IStatiPrenotazione[]>;

@Injectable({ providedIn: 'root' })
export class StatiPrenotazioneService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/stati-prenotaziones');

  create(statiPrenotazione: NewStatiPrenotazione): Observable<EntityResponseType> {
    return this.http.post<IStatiPrenotazione>(this.resourceUrl, statiPrenotazione, { observe: 'response' });
  }

  update(statiPrenotazione: IStatiPrenotazione): Observable<EntityResponseType> {
    return this.http.put<IStatiPrenotazione>(
      `${this.resourceUrl}/${this.getStatiPrenotazioneIdentifier(statiPrenotazione)}`,
      statiPrenotazione,
      { observe: 'response' },
    );
  }

  partialUpdate(statiPrenotazione: PartialUpdateStatiPrenotazione): Observable<EntityResponseType> {
    return this.http.patch<IStatiPrenotazione>(
      `${this.resourceUrl}/${this.getStatiPrenotazioneIdentifier(statiPrenotazione)}`,
      statiPrenotazione,
      { observe: 'response' },
    );
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http.get<IStatiPrenotazione>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IStatiPrenotazione[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getStatiPrenotazioneIdentifier(statiPrenotazione: Pick<IStatiPrenotazione, 'id'>): string {
    return statiPrenotazione.id;
  }

  compareStatiPrenotazione(o1: Pick<IStatiPrenotazione, 'id'> | null, o2: Pick<IStatiPrenotazione, 'id'> | null): boolean {
    return o1 && o2 ? this.getStatiPrenotazioneIdentifier(o1) === this.getStatiPrenotazioneIdentifier(o2) : o1 === o2;
  }

  addStatiPrenotazioneToCollectionIfMissing<Type extends Pick<IStatiPrenotazione, 'id'>>(
    statiPrenotazioneCollection: Type[],
    ...statiPrenotazionesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const statiPrenotaziones: Type[] = statiPrenotazionesToCheck.filter(isPresent);
    if (statiPrenotaziones.length > 0) {
      const statiPrenotazioneCollectionIdentifiers = statiPrenotazioneCollection.map(statiPrenotazioneItem =>
        this.getStatiPrenotazioneIdentifier(statiPrenotazioneItem),
      );
      const statiPrenotazionesToAdd = statiPrenotaziones.filter(statiPrenotazioneItem => {
        const statiPrenotazioneIdentifier = this.getStatiPrenotazioneIdentifier(statiPrenotazioneItem);
        if (statiPrenotazioneCollectionIdentifiers.includes(statiPrenotazioneIdentifier)) {
          return false;
        }
        statiPrenotazioneCollectionIdentifiers.push(statiPrenotazioneIdentifier);
        return true;
      });
      return [...statiPrenotazionesToAdd, ...statiPrenotazioneCollection];
    }
    return statiPrenotazioneCollection;
  }
}
