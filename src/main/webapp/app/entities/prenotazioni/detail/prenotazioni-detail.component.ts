import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatePipe } from 'app/shared/date';
import { IPrenotazioni } from '../prenotazioni.model';

@Component({
  selector: 'jhi-prenotazioni-detail',
  templateUrl: './prenotazioni-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatePipe],
})
export class PrenotazioniDetailComponent {
  prenotazioni = input<IPrenotazioni | null>(null);

  previousState(): void {
    window.history.back();
  }
}
