import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import EventiResolve from './route/eventi-routing-resolve.service';

const eventiRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/eventi.component').then(m => m.EventiComponent),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/eventi-detail.component').then(m => m.EventiDetailComponent),
    resolve: {
      eventi: EventiResolve,
    },
  },
  {
    path: 'new',
    loadComponent: () => import('./update/eventi-update.component').then(m => m.EventiUpdateComponent),
    resolve: {
      eventi: EventiResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/eventi-update.component').then(m => m.EventiUpdateComponent),
    resolve: {
      eventi: EventiResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default eventiRoute;
