import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ISalaDTO {
  id: string;
  nome: string;
  capienza: number;
  descrizione?: string;
  imageUrl?: string | null; // null = nessuna immagine → mostra placeholder
}

@Injectable({ providedIn: 'root' })
export class SaleApiService {
  private resourceUrl = '/api/sales';

  constructor(private http: HttpClient) {}

  getSaleDisponibili(data: string, inizio: string, fine: string, capienza: number): Observable<ISalaDTO[]> {
    const params = new HttpParams().set('data', data).set('inizio', inizio).set('fine', fine).set('capienza', capienza.toString());
    return this.http.get<ISalaDTO[]>(`${this.resourceUrl}/disponibili`, { params });
  }

  /**
   * Upload immagine per una sala (solo admin).
   * Ritorna il SaleDTO aggiornato con il nuovo imageUrl.
   */
  uploadImmagine(salaId: string, file: File): Observable<ISalaDTO> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ISalaDTO>(`${this.resourceUrl}/${salaId}/immagine`, formData);
  }

  /**
   * Rimuovi immagine di una sala (solo admin).
   */
  eliminaImmagine(salaId: string): Observable<void> {
    return this.http.delete<void>(`${this.resourceUrl}/${salaId}/immagine`);
  }
}
