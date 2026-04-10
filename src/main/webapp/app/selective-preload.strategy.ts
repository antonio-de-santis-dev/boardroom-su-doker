import { Injectable } from '@angular/core';
import { PreloadingStrategy, Route } from '@angular/router';
import { Observable, of, timer } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class SelectivePreloadStrategy implements PreloadingStrategy {
  preload(route: Route, load: () => Observable<unknown>): Observable<unknown> {
    const shouldPreload = route.data?.['preload'] === true;

    if (!shouldPreload) {
      return of(null); // skip — nessun preload
    }

    const delayMs: number = route.data?.['preloadDelay'] ?? 3000;

    // Ritarda il preload per non competere con le risorse del bootstrap
    return timer(delayMs).pipe(mergeMap(() => load()));
  }
}
