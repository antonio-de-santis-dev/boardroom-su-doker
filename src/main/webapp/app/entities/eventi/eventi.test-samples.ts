import { IEventi, NewEventi } from './eventi.model';

export const sampleWithRequiredData: IEventi = {
  id: '1d71039c-6525-4c46-85a1-f64b4a1aed8f',
  titolo: 'rebound so',
  tipo: 'PUBBLICO',
};

export const sampleWithPartialData: IEventi = {
  id: 'dd1568f0-5af5-468f-8650-3a1f75e9e8b8',
  titolo: 'everlasting',
  tipo: 'PRIVATO',
  prezzo: 3050.88,
};

export const sampleWithFullData: IEventi = {
  id: '7a6883d2-6835-4e73-bfab-635e2cc32dc6',
  titolo: 'blah gee',
  tipo: 'PRIVATO',
  prezzo: 21344,
};

export const sampleWithNewData: NewEventi = {
  titolo: 'rundown clean',
  tipo: 'PRIVATO',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
