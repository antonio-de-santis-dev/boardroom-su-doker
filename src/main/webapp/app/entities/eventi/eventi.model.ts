import { IPrenotazioni } from 'app/entities/prenotazioni/prenotazioni.model';
import { TipoEvento } from 'app/entities/enumerations/tipo-evento.model';

export interface IEventi {
  id: string;
  titolo?: string | null;
  tipo?: keyof typeof TipoEvento | null;
  prezzo?: number | null;
  descrizione?: string | null;
  data?: string | null;
  oraInizio?: string | null;
  oraFine?: string | null;
  salaNome?: string | null;
  salaImageUrl?: string | null;
  locandinaUrl?: string | null;
  prenotazione?: Pick<IPrenotazioni, 'id'> | null;
  numPersone?: number | null;
  postiOccupati?: number | null;
  eventoPieno?: boolean | null;
}

export type NewEventi = Omit<IEventi, 'id'> & { id: null };
