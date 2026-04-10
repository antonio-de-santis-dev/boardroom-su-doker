import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPrenotazioni } from '../prenotazioni.model';
import { PrenotazioniService } from '../service/prenotazioni.service';

@Component({
  templateUrl: './prenotazioni-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PrenotazioniDeleteDialogComponent {
  prenotazioni?: IPrenotazioni;

  protected prenotazioniService = inject(PrenotazioniService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.prenotazioniService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
