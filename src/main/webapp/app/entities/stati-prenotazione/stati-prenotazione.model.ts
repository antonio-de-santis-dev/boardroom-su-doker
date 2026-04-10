import { StatoCodice } from 'app/entities/enumerations/stato-codice.model';

export interface IStatiPrenotazione {
  id: string;
  descrizione?: string | null;
  codice?: keyof typeof StatoCodice | null;
  ordineAzione?: number | null;
}

export type NewStatiPrenotazione = Omit<IStatiPrenotazione, 'id'> & { id: null };
