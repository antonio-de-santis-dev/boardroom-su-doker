import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { StatoCodice } from 'app/entities/enumerations/stato-codice.model';
import { IStatiPrenotazione } from '../stati-prenotazione.model';
import { StatiPrenotazioneService } from '../service/stati-prenotazione.service';
import { StatiPrenotazioneFormGroup, StatiPrenotazioneFormService } from './stati-prenotazione-form.service';

@Component({
  selector: 'jhi-stati-prenotazione-update',
  templateUrl: './stati-prenotazione-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class StatiPrenotazioneUpdateComponent implements OnInit {
  isSaving = false;
  statiPrenotazione: IStatiPrenotazione | null = null;
  statoCodiceValues = Object.keys(StatoCodice);

  protected statiPrenotazioneService = inject(StatiPrenotazioneService);
  protected statiPrenotazioneFormService = inject(StatiPrenotazioneFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: StatiPrenotazioneFormGroup = this.statiPrenotazioneFormService.createStatiPrenotazioneFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ statiPrenotazione }) => {
      this.statiPrenotazione = statiPrenotazione;
      if (statiPrenotazione) {
        this.updateForm(statiPrenotazione);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const statiPrenotazione = this.statiPrenotazioneFormService.getStatiPrenotazione(this.editForm);
    if (statiPrenotazione.id !== null) {
      this.subscribeToSaveResponse(this.statiPrenotazioneService.update(statiPrenotazione));
    } else {
      this.subscribeToSaveResponse(this.statiPrenotazioneService.create(statiPrenotazione));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IStatiPrenotazione>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(statiPrenotazione: IStatiPrenotazione): void {
    this.statiPrenotazione = statiPrenotazione;
    this.statiPrenotazioneFormService.resetForm(this.editForm, statiPrenotazione);
  }
}
