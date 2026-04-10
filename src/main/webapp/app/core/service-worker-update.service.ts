import { Injectable, inject } from '@angular/core';
import { SwUpdate, VersionReadyEvent } from '@angular/service-worker';
import { filter } from 'rxjs/operators';

/**
 * FIX NAVBAR — Service Worker auto-update
 *
 * Quando Angular trova una nuova versione dell'app (nuovo build Docker),
 * attiva il nuovo SW e ricarica la pagina automaticamente.
 * Questo elimina il problema della navbar rotta al primo avvio dopo un rebuild:
 * il browser smette di servire CSS/JS vecchi dalla cache del SW.
 */
@Injectable({ providedIn: 'root' })
export class ServiceWorkerUpdateService {
  private readonly swUpdate = inject(SwUpdate);

  constructor() {
    if (!this.swUpdate.isEnabled) {
      return;
    }

    // Quando il SW scarica una nuova versione → attivala e ricarica la pagina
    this.swUpdate.versionUpdates
      .pipe(filter((evt): evt is VersionReadyEvent => evt.type === 'VERSION_READY'))
      .subscribe(() => {
        this.swUpdate.activateUpdate().then(() => {
          document.location.reload();
        });
      });

    // Controlla aggiornamenti ogni 60 secondi (utile dopo un rebuild Docker)
    setInterval(() => {
      this.swUpdate.checkForUpdate().catch(() => {
        // ignora errori di rete durante il check
      });
    }, 60_000);
  }
}
