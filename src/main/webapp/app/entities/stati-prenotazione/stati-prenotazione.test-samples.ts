import { IStatiPrenotazione, NewStatiPrenotazione } from './stati-prenotazione.model';

export const sampleWithRequiredData: IStatiPrenotazione = {
  id: '9c0a8de1-4940-416c-bdb2-5e019359dfd7',
  descrizione: 'absentmindedly yippee ponder',
  codice: 'CONFIRMED',
  ordineAzione: 30675,
};

export const sampleWithPartialData: IStatiPrenotazione = {
  id: 'dc741f19-635d-46c2-843c-748a2f49288c',
  descrizione: 'under',
  codice: 'CONFIRMED',
  ordineAzione: 12486,
};

export const sampleWithFullData: IStatiPrenotazione = {
  id: 'fd12bf76-1fd2-4b63-9920-d42138e646a0',
  descrizione: 'next joyfully',
  codice: 'CANCELLED',
  ordineAzione: 32334,
};

export const sampleWithNewData: NewStatiPrenotazione = {
  descrizione: 'separately probe',
  codice: 'REJECTED',
  ordineAzione: 24075,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
