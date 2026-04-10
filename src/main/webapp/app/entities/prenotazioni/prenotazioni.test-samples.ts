import dayjs from 'dayjs/esm';

import { IPrenotazioni, NewPrenotazioni } from './prenotazioni.model';

export const sampleWithRequiredData: IPrenotazioni = {
  id: 'e7c82087-8c09-4223-97e2-46ae8d73f65e',
  data: dayjs('2026-01-20'),
  oraInizio: '23:35:00',
  oraFine: '02:18:00',
};

export const sampleWithPartialData: IPrenotazioni = {
  id: '769a6afa-ff68-4506-a645-34a9809b8bd5',
  data: dayjs('2026-01-20'),
  oraInizio: '05:24:00',
  oraFine: '01:02:00',
};

export const sampleWithFullData: IPrenotazioni = {
  id: 'e3f57121-2d3d-4d26-915d-13aac8ad5000',
  data: dayjs('2026-01-20'),
  oraInizio: '22:06:00',
  oraFine: '20:39:00',
};

export const sampleWithNewData: NewPrenotazioni = {
  data: dayjs('2026-01-21'),
  oraInizio: '19:38:00',
  oraFine: '06:19:00',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
