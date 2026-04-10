import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import SaleResolve from './route/sale-routing-resolve.service';

const saleRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/sale.component').then(m => m.SaleComponent),
    data: {},
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/sale-detail.component').then(m => m.SaleDetailComponent),
    resolve: {
      sale: SaleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/sale-update.component').then(m => m.SaleUpdateComponent),
    resolve: {
      sale: SaleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/sale-update.component').then(m => m.SaleUpdateComponent),
    resolve: {
      sale: SaleResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default saleRoute;
