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

/**
 * FIX #04 — Aggiunto SelectivePreloadStrategy al posto di nessun preloading.
 * FIX #05 — Confermato: withDebugTracing() è già correttamente gated su
 *            environment.DEBUG_INFO_ENABLED. Nessuna modifica necessaria.
 *
 * IMPORT PATH: adattare in base alla struttura del progetto.
 * Esempio: './core/routing/selective-preload.strategy'
 * oppure direttamente './selective-preload.strategy' se nella stessa cartella.
 */
import { SelectivePreloadStrategy } from './selective-preload.strategy';

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

  // FIX #04: preloading selettivo — solo i chunk marcati con data.preload=true
  // vengono precaricati (con delay), gli altri solo on-demand.
  withPreloading(SelectivePreloadStrategy),
];

// FIX #05: CORRETTO — withDebugTracing() già gated su DEBUG_INFO_ENABLED.
// In produzione environment.DEBUG_INFO_ENABLED = false → nessun overhead di tracing.
if (environment.DEBUG_INFO_ENABLED) {
  routerFeatures.push(withDebugTracing());
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, ...routerFeatures),

    importProvidersFrom(
      ServiceWorkerModule.register('ngsw-worker.js', {
        // Service Worker abilitato SOLO in produzione (DEBUG_INFO_ENABLED = false in prod)
        enabled: !environment.DEBUG_INFO_ENABLED,
      }),
    ),

    provideHttpClient(withInterceptors([loadingInterceptor]), withInterceptorsFromDi()),

    Title,
    { provide: LOCALE_ID, useValue: 'it' },
    { provide: NgbDateAdapter, useClass: NgbDateDayjsAdapter },

    httpInterceptorProviders,

    { provide: TitleStrategy, useClass: AppPageTitleStrategy },

    // FIX #04: registrazione SelectivePreloadStrategy come provider
    SelectivePreloadStrategy,
  ],
};
