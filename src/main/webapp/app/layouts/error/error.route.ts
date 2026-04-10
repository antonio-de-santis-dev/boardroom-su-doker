import { Routes } from '@angular/router';

export const errorRoute: Routes = [
  {
    path: 'error',
    loadComponent: () => import('./error.component'),
    title: 'Pagina di errore!',
  },
  {
    path: 'accessdenied',
    loadComponent: () => import('./error.component'),
    data: {
      errorMessage: 'Non si dispongono i privilegi sufficienti per accedere a qusta pagina.',
    },
    title: 'Pagina di errore!',
  },
  {
    path: '404',
    loadComponent: () => import('./error.component'),
    data: {
      errorMessage: 'La pagina non esiste.',
    },
    title: 'Pagina di errore!',
  },
  {
    path: '**',
    redirectTo: '/404',
  },
];
