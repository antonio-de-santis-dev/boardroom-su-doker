import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'sale',
    data: { pageTitle: 'Sales' },
    loadChildren: () => import('./sale/sale.routes'),
  },
  {
    path: 'utenti',
    data: { pageTitle: 'Utentis' },
    loadChildren: () => import('./utenti/utenti.routes'),
  },
  {
    path: 'stati-prenotazione',
    data: { pageTitle: 'StatiPrenotaziones' },
    loadChildren: () => import('./stati-prenotazione/stati-prenotazione.routes'),
  },
  {
    path: 'prenotazioni',
    data: { pageTitle: 'Prenotazionis' },
    loadChildren: () => import('./prenotazioni/prenotazioni.routes'),
  },
  {
    path: 'eventi',
    data: { pageTitle: 'Eventis' },
    loadChildren: () => import('./eventi/eventi.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
