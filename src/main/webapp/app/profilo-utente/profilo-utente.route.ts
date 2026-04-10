import { Route } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

export const PROFILO_UTENTE_ROUTE: Route = {
  path: 'profilo-utente',
  loadComponent: () => import('./profilo-utente.component').then(m => m.ProfiloUtenteComponent),
  data: { pageTitle: 'Il mio profilo' },
  canActivate: [UserRouteAccessService],
};
