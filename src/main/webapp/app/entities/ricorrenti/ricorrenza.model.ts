export type Frequenza = 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY';

export type GiornoSettimana = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface IRicorrenza {
  id?: string;
  salaId: string;
  salaNome?: string;
  frequenza: Frequenza;
  giorniSettimana?: GiornoSettimana[];
  dataInizio: string; // YYYY-MM-DD
  dataFine?: string; // YYYY-MM-DD
  numOccorrenze?: number;
  oraInizio: string; // HH:mm
  oraFine: string; // HH:mm
  numPersone?: number;
  // Titolo e descrizione da propagare a tutte le istanze della serie
  titoloEvento?: string;
  descrizioneEvento?: string;
  // Campi risposta
  istanzeCreate?: number;
  istanzeConflitto?: number;
  dateConflitto?: string[];
}

export const GIORNI_OPTIONS: { value: GiornoSettimana; label: string }[] = [
  { value: 'MONDAY', label: 'Lunedì' },
  { value: 'TUESDAY', label: 'Martedì' },
  { value: 'WEDNESDAY', label: 'Mercoledì' },
  { value: 'THURSDAY', label: 'Giovedì' },
  { value: 'FRIDAY', label: 'Venerdì' },
  { value: 'SATURDAY', label: 'Sabato' },
  { value: 'SUNDAY', label: 'Domenica' },
];

export const FREQUENZA_OPTIONS: { value: Frequenza; label: string; desc: string }[] = [
  { value: 'WEEKLY', label: 'Settimanale', desc: 'Ogni settimana nei giorni scelti' },
  { value: 'BIWEEKLY', label: 'Bisettimanale', desc: 'Ogni due settimane nei giorni scelti' },
  { value: 'MONTHLY', label: 'Mensile', desc: 'Stesso giorno ogni mese' },
];
