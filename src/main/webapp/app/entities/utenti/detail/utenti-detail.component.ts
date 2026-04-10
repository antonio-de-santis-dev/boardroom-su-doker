import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IUtenti } from '../utenti.model';

@Component({
  selector: 'jhi-utenti-detail',
  templateUrl: './utenti-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class UtentiDetailComponent {
  utenti = input<IUtenti | null>(null);

  previousState(): void {
    window.history.back();
  }
}
