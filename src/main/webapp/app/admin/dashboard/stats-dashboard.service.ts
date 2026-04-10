import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface LabelValue {
  label: string;
  value: number;
}
export interface LabelDouble {
  label: string;
  value: number;
}

export interface StatsDashboard {
  totalePrenotazioni: number;
  prenotazioniConfermate: number;
  tassoOccupazionePercentuale: number;
  utentiAttivi: number;
  prenotazioniPerSala: LabelValue[];
  oreRichieste: LabelValue[];
  occupazionePerMese: LabelDouble[];
  topUtenti: LabelValue[];
  prenotazioniPerGiorno: LabelValue[];
}

export interface SalaAdmin {
  id?: string;
  nome: string;
  capienza: number;
  descrizione?: string;
}

export type Periodo = '1m' | '3m' | '1y';

@Injectable({ providedIn: 'root' })
export class StatsDashboardService {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  private readonly baseUrl = this.appConfig.getEndpointFor('api/admin');

  getStats(periodo: Periodo = '1m'): Observable<StatsDashboard> {
    const params = new HttpParams().set('periodo', periodo);
    return this.http.get<StatsDashboard>(`${this.baseUrl}/stats`, { params });
  }

  getSale(): Observable<SalaAdmin[]> {
    return this.http.get<SalaAdmin[]>(`${this.baseUrl}/sale`);
  }

  creaSala(dto: SalaAdmin): Observable<SalaAdmin> {
    return this.http.post<SalaAdmin>(`${this.baseUrl}/sale`, dto);
  }

  modificaSala(id: string, dto: SalaAdmin): Observable<SalaAdmin> {
    return this.http.put<SalaAdmin>(`${this.baseUrl}/sale/${id}`, dto);
  }

  eliminaSala(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/sale/${id}`);
  }
}
