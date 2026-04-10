import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IStatiPrenotazione } from '../stati-prenotazione.model';

@Component({
  selector: 'jhi-stati-prenotazione-detail',
  templateUrl: './stati-prenotazione-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class StatiPrenotazioneDetailComponent {
  statiPrenotazione = input<IStatiPrenotazione | null>(null);

  previousState(): void {
    window.history.back();
  }
}
