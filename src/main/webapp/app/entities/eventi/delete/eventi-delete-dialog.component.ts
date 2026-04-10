import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IEventi } from '../eventi.model';
import { EventiService } from '../service/eventi.service';

@Component({
  templateUrl: './eventi-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class EventiDeleteDialogComponent {
  eventi?: IEventi;

  protected eventiService = inject(EventiService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.eventiService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
