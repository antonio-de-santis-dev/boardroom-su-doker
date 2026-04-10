import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import PrenotazioniResolve from './route/prenotazioni-routing-resolve.service';

const prenotazioniRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/prenotazioni.component').then(m => m.PrenotazioniComponent),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  // ── Vista Calendario (nuova) ──────────────────────────────
  {
    path: 'calendar',
    loadComponent: () => import('./calendar/prenotazioni-calendar.component').then(m => m.PrenotazioniCalendarComponent),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  // ─────────────────────────────────────────────────────────
  {
    path: ':id/view',
    loadComponent: () => import('./detail/prenotazioni-detail.component').then(m => m.PrenotazioniDetailComponent),
    resolve: {
      prenotazioni: PrenotazioniResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/prenotazioni-update.component').then(m => m.PrenotazioniUpdateComponent),
    resolve: {
      prenotazioni: PrenotazioniResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/prenotazioni-update.component').then(m => m.PrenotazioniUpdateComponent),
    resolve: {
      prenotazioni: PrenotazioniResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default prenotazioniRoute;
