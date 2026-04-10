import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IRicorrenza } from './ricorrenza.model';

@Injectable({ providedIn: 'root' })
export class RicorrenzaService {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  private readonly url = this.appConfig.getEndpointFor('api/ricorrenze');

  /** Crea una nuova serie ricorrente */
  crea(dto: IRicorrenza): Observable<IRicorrenza> {
    return this.http.post<IRicorrenza>(this.url, dto);
  }

  /** Recupera le serie dell'utente (admin: tutte) */
  getMie(): Observable<IRicorrenza[]> {
    return this.http.get<IRicorrenza[]>(this.url);
  }

  /** Cancella le istanze future della serie */
  cancellaFuture(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}/future`);
  }

  /** Cancella tutta la serie */
  cancellaTutta(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
