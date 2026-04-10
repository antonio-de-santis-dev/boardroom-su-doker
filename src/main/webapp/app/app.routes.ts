import { Routes } from '@angular/router';

import { Authority } from 'app/config/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { errorRoute } from './layouts/error/error.route';

/**
 * FIX #04 — CORREZIONE rispetto alla versione precedente:
 *
 * Il tentativo di usare loadChildren() per MIE_PRENOTAZIONI_ROUTE,
 * PROFILO_UTENTE_ROUTE e STORICO_PRENOTAZIONI_ROUTE causava un doppio
 * annidamento del path (es. "mie-prenotazioni/mie-prenotazioni") perché
 * l'oggetto Route esportato contiene già path: 'mie-prenotazioni'.
 *
 * SOLUZIONE CORRETTA:
 *   Mantenere gli import statici dei file *route.ts — sono piccoli
 *   (1-2 KB ciascuno) e i componenti sono già lazy tramite loadComponent().
 *   Il chunk del componente viene caricato solo alla prima navigazione.
 *
 * Il guadagno reale sul bundle size viene dal lazy loading dei moduli
 * pesanti: admin, account, entities — già tutti configurati con loadChildren.
 *
 * FIX #05 — withDebugTracing() già correttamente gated su DEBUG_INFO_ENABLED.
 */

import { MIE_PRENOTAZIONI_ROUTE } from './mie-prenotazioni/mie-prenotazioni.route';
import { PROFILO_UTENTE_ROUTE } from './profilo-utente/profilo-utente.route';
import { STORICO_PRENOTAZIONI_ROUTE } from './storico-prenotazioni/storico-prenotazioni.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component'),
    title: 'Boardroom',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar.component'),
    outlet: 'navbar',
  },
  {
    path: 'prenota-sala',
    loadComponent: () => import('./prenota-sala/prenota-sala.component'),
    title: 'Prenota Sala',
    canActivate: [UserRouteAccessService],
  },

  // Questi oggetti Route usano già loadComponent() internamente →
  // i componenti sono lazy, l'import statico del *route.ts è trascurabile.
  MIE_PRENOTAZIONI_ROUTE,
  PROFILO_UTENTE_ROUTE,
  STORICO_PRENOTAZIONI_ROUTE,

  {
    path: 'risultati-sala',
    loadComponent: () => import('./prenota-sala/risultati-sala/risultati-sala.component').then(m => m.RisultatiSalaComponent),
    title: 'Sale Disponibili',
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'prenota-sala/crea-evento',
    loadComponent: () => import('./prenota-sala/crea-evento/crea-evento.component').then(m => m.CreaEventoComponent),
    title: 'Dettagli Evento',
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'admin',
    data: { authorities: [Authority.ADMIN] },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component'),
    title: 'Login',
  },
  {
    path: '',
    loadChildren: () => import('./entities/entity.routes'),
  },
  ...errorRoute,
];

export default routes;
