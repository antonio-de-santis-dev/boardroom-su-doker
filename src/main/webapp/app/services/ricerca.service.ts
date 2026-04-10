import { Injectable } from '@angular/core';

export interface IRicercaSale {
  data: string;
  ora: string;
  capienza: number | null;
}

@Injectable({
  providedIn: 'root',
})
export class RicercaService {
  private datiRicerca: IRicercaSale = {
    data: '',
    ora: '',
    capienza: null,
  };

  salvaRicerca(nuoviDati: IRicercaSale): void {
    this.datiRicerca = { ...nuoviDati };
  }

  recuperaRicerca(): IRicercaSale {
    return { ...this.datiRicerca };
  }

  resetRicerca(): void {
    this.datiRicerca = {
      data: '',
      ora: '',
      capienza: null,
    };
  }
}
