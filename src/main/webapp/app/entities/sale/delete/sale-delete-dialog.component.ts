import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ISale } from '../sale.model';
import { SaleService } from '../service/sale.service';

@Component({
  templateUrl: './sale-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class SaleDeleteDialogComponent {
  sale?: ISale;

  protected saleService = inject(SaleService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.saleService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
