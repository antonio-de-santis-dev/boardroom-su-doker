import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPrenotazioni, NewPrenotazioni } from '../prenotazioni.model';

export type PartialUpdatePrenotazioni = Partial<IPrenotazioni> & Pick<IPrenotazioni, 'id'>;

type RestOf<T extends IPrenotazioni | NewPrenotazioni> = Omit<T, 'data'> & {
  data?: string | null;
};

export type RestPrenotazioni = RestOf<IPrenotazioni>;

export type NewRestPrenotazioni = RestOf<NewPrenotazioni>;

export type PartialUpdateRestPrenotazioni = RestOf<PartialUpdatePrenotazioni>;

export type EntityResponseType = HttpResponse<IPrenotazioni>;
export type EntityArrayResponseType = HttpResponse<IPrenotazioni[]>;

@Injectable({ providedIn: 'root' })
export class PrenotazioniService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/prenotazionis');

  create(prenotazioni: NewPrenotazioni): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(prenotazioni);
    return this.http
      .post<RestPrenotazioni>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(prenotazioni: IPrenotazioni): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(prenotazioni);
    return this.http
      .put<RestPrenotazioni>(`${this.resourceUrl}/${this.getPrenotazioniIdentifier(prenotazioni)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(prenotazioni: PartialUpdatePrenotazioni): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(prenotazioni);
    return this.http
      .patch<RestPrenotazioni>(`${this.resourceUrl}/${this.getPrenotazioniIdentifier(prenotazioni)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http
      .get<RestPrenotazioni>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPrenotazioni[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  getStorico(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPrenotazioni[]>(`${this.resourceUrl}/storico`, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getPrenotazioniIdentifier(prenotazioni: Pick<IPrenotazioni, 'id'>): string {
    return prenotazioni.id;
  }

  comparePrenotazioni(o1: Pick<IPrenotazioni, 'id'> | null, o2: Pick<IPrenotazioni, 'id'> | null): boolean {
    return o1 && o2 ? this.getPrenotazioniIdentifier(o1) === this.getPrenotazioniIdentifier(o2) : o1 === o2;
  }

  addPrenotazioniToCollectionIfMissing<Type extends Pick<IPrenotazioni, 'id'>>(
    prenotazioniCollection: Type[],
    ...prenotazionisToCheck: (Type | null | undefined)[]
  ): Type[] {
    const prenotazionis: Type[] = prenotazionisToCheck.filter(isPresent);
    if (prenotazionis.length > 0) {
      const prenotazioniCollectionIdentifiers = prenotazioniCollection.map(prenotazioniItem =>
        this.getPrenotazioniIdentifier(prenotazioniItem),
      );
      const prenotazionisToAdd = prenotazionis.filter(prenotazioniItem => {
        const prenotazioniIdentifier = this.getPrenotazioniIdentifier(prenotazioniItem);
        if (prenotazioniCollectionIdentifiers.includes(prenotazioniIdentifier)) {
          return false;
        }
        prenotazioniCollectionIdentifiers.push(prenotazioniIdentifier);
        return true;
      });
      return [...prenotazionisToAdd, ...prenotazioniCollection];
    }
    return prenotazioniCollection;
  }

  protected convertDateFromClient<T extends IPrenotazioni | NewPrenotazioni | PartialUpdatePrenotazioni>(prenotazioni: T): RestOf<T> {
    return {
      ...prenotazioni,
      data: prenotazioni.data?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restPrenotazioni: RestPrenotazioni): IPrenotazioni {
    return {
      ...restPrenotazioni,
      data: restPrenotazioni.data ? dayjs(restPrenotazioni.data) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestPrenotazioni>): HttpResponse<IPrenotazioni> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestPrenotazioni[]>): HttpResponse<IPrenotazioni[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
