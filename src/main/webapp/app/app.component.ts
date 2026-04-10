import { Component, inject } from '@angular/core';
import { registerLocaleData, CommonModule } from '@angular/common';
import dayjs from 'dayjs/esm';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { NgbDatepickerConfig } from '@ng-bootstrap/ng-bootstrap';
import locale from '@angular/common/locales/it';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { fontAwesomeIcons } from './config/font-awesome-icons';
import MainComponent from './layouts/main/main.component';
import { NotificationComponent } from 'app/shared/notification/notification.component';
import { ServiceWorkerUpdateService } from './core/service-worker-update.service';

@Component({
  selector: 'jhi-app',
  standalone: true,
  template: `
    <jhi-main></jhi-main>
    <jhi-notification></jhi-notification>
  `,
  imports: [CommonModule, MainComponent, NotificationComponent],
})
export default class AppComponent {
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly iconLibrary = inject(FaIconLibrary);
  private readonly dpConfig = inject(NgbDatepickerConfig);

  // FIX NAVBAR: inject del service per attivare l'auto-update del Service Worker.
  // Il semplice inject è sufficiente per inizializzare il service e i suoi listener.
  private readonly swUpdateService = inject(ServiceWorkerUpdateService);

  constructor() {
    this.applicationConfigService.setEndpointPrefix(SERVER_API_URL);
    registerLocaleData(locale);
    this.iconLibrary.addIcons(...fontAwesomeIcons);
    this.dpConfig.minDate = { year: dayjs().subtract(100, 'year').year(), month: 1, day: 1 };
  }
}
