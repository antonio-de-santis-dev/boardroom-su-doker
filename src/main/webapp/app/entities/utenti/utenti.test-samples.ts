import { IUtenti, NewUtenti } from './utenti.model';

export const sampleWithRequiredData: IUtenti = {
  id: '417e30fb-8dae-47ee-984c-af166c5c305d',
  nome: 'fondly',
  numeroDiTelefono: 'whoever',
};

export const sampleWithPartialData: IUtenti = {
  id: '315b3508-46a0-4e96-9368-5ccdaaf27845',
  nome: 'upside-down frantically',
  numeroDiTelefono: 'careless',
};

export const sampleWithFullData: IUtenti = {
  id: '4c4c1a95-e5ac-4998-a31e-845f1c001bd8',
  nome: 'shrilly',
  nomeAzienda: 'foolishly vice tomatillo',
  numeroDiTelefono: 'yahoo',
};

export const sampleWithNewData: NewUtenti = {
  nome: 'anti',
  numeroDiTelefono: 'as acclaimed as',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
