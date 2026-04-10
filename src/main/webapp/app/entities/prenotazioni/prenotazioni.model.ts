import dayjs from 'dayjs/esm';
import { IStatiPrenotazione } from 'app/entities/stati-prenotazione/stati-prenotazione.model';
import { IUtenti } from 'app/entities/utenti/utenti.model';
import { ISale } from 'app/entities/sale/sale.model';

export interface IPrenotazioni {
  id: string;
  data?: dayjs.Dayjs | null;
  oraInizio?: string | null;
  oraFine?: string | null;
  stato?: Pick<IStatiPrenotazione, 'id' | 'codice'> | null;
  utente?: Pick<IUtenti, 'id' | 'nome'> | null;
  sala?: Pick<ISale, 'id' | 'nome'> | null;
}

export type NewPrenotazioni = Omit<IPrenotazioni, 'id'> & { id: null };
