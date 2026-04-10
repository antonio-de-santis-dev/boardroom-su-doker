import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import StatiPrenotazioneResolve from './route/stati-prenotazione-routing-resolve.service';

const statiPrenotazioneRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/stati-prenotazione.component').then(m => m.StatiPrenotazioneComponent),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/stati-prenotazione-detail.component').then(m => m.StatiPrenotazioneDetailComponent),
    resolve: {
      statiPrenotazione: StatiPrenotazioneResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/stati-prenotazione-update.component').then(m => m.StatiPrenotazioneUpdateComponent),
    resolve: {
      statiPrenotazione: StatiPrenotazioneResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/stati-prenotazione-update.component').then(m => m.StatiPrenotazioneUpdateComponent),
    resolve: {
      statiPrenotazione: StatiPrenotazioneResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default statiPrenotazioneRoute;
