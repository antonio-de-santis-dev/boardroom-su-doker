import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import UtentiResolve from './route/utenti-routing-resolve.service';

const utentiRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/utenti.component').then(m => m.UtentiComponent),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/utenti-detail.component').then(m => m.UtentiDetailComponent),
    resolve: {
      utenti: UtentiResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/utenti-update.component').then(m => m.UtentiUpdateComponent),
    resolve: {
      utenti: UtentiResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/utenti-update.component').then(m => m.UtentiUpdateComponent),
    resolve: {
      utenti: UtentiResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default utentiRoute;
