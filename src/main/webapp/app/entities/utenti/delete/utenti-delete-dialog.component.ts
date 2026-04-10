import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IUtenti } from '../utenti.model';
import { UtentiService } from '../service/utenti.service';

@Component({
  templateUrl: './utenti-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class UtentiDeleteDialogComponent {
  utenti?: IUtenti;

  protected utentiService = inject(UtentiService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.utentiService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
