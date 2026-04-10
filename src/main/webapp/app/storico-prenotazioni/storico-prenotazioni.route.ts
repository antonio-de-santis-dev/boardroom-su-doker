import { Route } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

export const STORICO_PRENOTAZIONI_ROUTE: Route = {
  path: 'storico-prenotazioni',
  loadComponent: () => import('./storico-prenotazioni.component').then(m => m.StoricoPrenotazioniComponent),
  data: {
    pageTitle: 'Storico Prenotazioni',
  },
  canActivate: [UserRouteAccessService],
};
