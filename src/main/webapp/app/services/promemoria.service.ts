import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type TipoPromemoria = 'WEEK_BEFORE' | 'TWO_DAYS_BEFORE' | 'DAY_BEFORE' | 'SAME_DAY';

export interface TipoStatoDTO {
  tipo: TipoPromemoria;
  etichetta: string;
  abilitato: boolean;
  inviato: boolean;
}

export interface PromemoriaDTO {
  prenotazioneId: string;
  tipiAbilitati: TipoPromemoria[];
  riepilogo: TipoStatoDTO[];
}

@Injectable({ providedIn: 'root' })
export class PromemoriaService {
  private http = inject(HttpClient);
  private readonly url = '/api/promemoria';

  get(prenotazioneId: string): Observable<PromemoriaDTO> {
    return this.http.get<PromemoriaDTO>(`${this.url}/${prenotazioneId}`);
  }

  salva(dto: { prenotazioneId: string; tipiAbilitati: TipoPromemoria[] }): Observable<PromemoriaDTO> {
    return this.http.post<PromemoriaDTO>(this.url, dto);
  }
}
