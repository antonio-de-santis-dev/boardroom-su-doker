import { ISale, NewSale } from './sale.model';

export const sampleWithRequiredData: ISale = {
  id: '1615bdd2-92f7-48fb-a432-bbd7722adb9b',
  nome: 'tepid rare poppy',
  capienza: 31105,
};

export const sampleWithPartialData: ISale = {
  id: 'd65ac06c-5004-4cec-bdc2-95319a426d3a',
  nome: 'what helpfully',
  capienza: 3154,
};

export const sampleWithFullData: ISale = {
  id: 'e0a19f3a-af4f-4e15-9bb4-047f1b04539d',
  nome: 'bungalow meanwhile fuzzy',
  capienza: 25945,
  descrizione: 'because defiantly if',
};

export const sampleWithNewData: NewSale = {
  nome: 'delete',
  capienza: 1225,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
