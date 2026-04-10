import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IStatiPrenotazione } from '../stati-prenotazione.model';
import { StatiPrenotazioneService } from '../service/stati-prenotazione.service';

@Component({
  templateUrl: './stati-prenotazione-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class StatiPrenotazioneDeleteDialogComponent {
  statiPrenotazione?: IStatiPrenotazione;

  protected statiPrenotazioneService = inject(StatiPrenotazioneService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.statiPrenotazioneService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
