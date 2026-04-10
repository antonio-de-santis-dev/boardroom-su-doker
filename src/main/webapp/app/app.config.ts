import { ApplicationConfig, LOCALE_ID, importProvidersFrom, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import {
  NavigationError,
  Router,
  RouterFeatures,
  TitleStrategy,
  provideRouter,
  withComponentInputBinding,
  withDebugTracing,
  withNavigationErrorHandler,
  withPreloading,
} from '@angular/router';
import { ServiceWorkerModule } from '@angular/service-worker';
import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';

import { NgbDateAdapter } from '@ng-bootstrap/ng-bootstrap';

import './config/dayjs';
import { environment } from 'environments/environment';
import { httpInterceptorProviders } from './core/interceptor';
import routes from './app.routes';

import { NgbDateDayjsAdapter } from './config/datepicker-adapter';
import { AppPageTitleStrategy } from './app-page-title-strategy';
import { loadingInterceptor } from './core/util/loading.interceptor';
import { SelectivePreloadStrategy } from './selective-preload.strategy';
import { ServiceWorkerUpdateService } from './core/service-worker-update.service';

const routerFeatures: RouterFeatures[] = [
  withComponentInputBinding(),
  withNavigationErrorHandler((e: NavigationError) => {
    const router = inject(Router);
    if (e?.error?.status === 403) {
      router.navigate(['/accessdenied']);
    } else if (e?.error?.status === 404) {
      router.navigate(['/404']);
    } else if (e?.error?.status === 401) {
      router.navigate(['/login']);
    } else {
      router.navigate(['/error']);
    }
  }),

  withPreloading(SelectivePreloadStrategy),
];

if (environment.DEBUG_INFO_ENABLED) {
  routerFeatures.push(withDebugTracing());
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, ...routerFeatures),

    importProvidersFrom(
      ServiceWorkerModule.register('ngsw-worker.js', {
        // Service Worker abilitato SOLO in produzione
        enabled: !environment.DEBUG_INFO_ENABLED,
        // Registra il SW solo dopo che l'app è stabile (evita rallentamenti allo startup)
        registrationStrategy: 'registerWhenStable:30000',
      }),
    ),

    provideHttpClient(withInterceptors([loadingInterceptor]), withInterceptorsFromDi()),

    Title,
    { provide: LOCALE_ID, useValue: 'it' },
    { provide: NgbDateAdapter, useClass: NgbDateDayjsAdapter },

    httpInterceptorProviders,

    { provide: TitleStrategy, useClass: AppPageTitleStrategy },

    SelectivePreloadStrategy,

    // FIX NAVBAR: auto-aggiornamento SW dopo rebuild Docker
    ServiceWorkerUpdateService,
  ],
};
