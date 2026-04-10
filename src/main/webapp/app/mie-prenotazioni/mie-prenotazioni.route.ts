import { Route } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

export const MIE_PRENOTAZIONI_ROUTE: Route = {
  path: 'mie-prenotazioni',
  loadComponent: () => import('./mie-prenotazioni.component').then(m => m.MiePrenotazioniComponent),
  canActivate: [UserRouteAccessService],
  data: {
    pageTitle: 'Le mie prenotazioni',
  },
};
