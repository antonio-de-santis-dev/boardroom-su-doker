import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface EventoDTO {
  id: string;
}

@Injectable({ providedIn: 'root' })
export class EventiApiService {
  private baseUrl = '/api/eventis';

  constructor(private http: HttpClient) {}

  creaEvento(payload: any): Observable<EventoDTO> {
    return this.http.post<EventoDTO>(this.baseUrl, payload);
  }
}
