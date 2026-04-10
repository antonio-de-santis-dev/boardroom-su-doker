import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IStatiPrenotazione } from 'app/entities/stati-prenotazione/stati-prenotazione.model';
import { StatiPrenotazioneService } from 'app/entities/stati-prenotazione/service/stati-prenotazione.service';
import { IUtenti } from 'app/entities/utenti/utenti.model';
import { UtentiService } from 'app/entities/utenti/service/utenti.service';
import { ISale } from 'app/entities/sale/sale.model';
import { SaleService } from 'app/entities/sale/service/sale.service';
import { PrenotazioniService } from '../service/prenotazioni.service';
import { IPrenotazioni } from '../prenotazioni.model';
import { PrenotazioniFormGroup, PrenotazioniFormService } from './prenotazioni-form.service';

@Component({
  selector: 'jhi-prenotazioni-update',
  templateUrl: './prenotazioni-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PrenotazioniUpdateComponent implements OnInit {
  isSaving = false;
  prenotazioni: IPrenotazioni | null = null;

  statiPrenotazionesSharedCollection: IStatiPrenotazione[] = [];
  utentisSharedCollection: IUtenti[] = [];
  salesSharedCollection: ISale[] = [];

  protected prenotazioniService = inject(PrenotazioniService);
  protected prenotazioniFormService = inject(PrenotazioniFormService);
  protected statiPrenotazioneService = inject(StatiPrenotazioneService);
  protected utentiService = inject(UtentiService);
  protected saleService = inject(SaleService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PrenotazioniFormGroup = this.prenotazioniFormService.createPrenotazioniFormGroup();

  compareStatiPrenotazione = (o1: IStatiPrenotazione | null, o2: IStatiPrenotazione | null): boolean =>
    this.statiPrenotazioneService.compareStatiPrenotazione(o1, o2);

  compareUtenti = (o1: IUtenti | null, o2: IUtenti | null): boolean => this.utentiService.compareUtenti(o1, o2);

  compareSale = (o1: ISale | null, o2: ISale | null): boolean => this.saleService.compareSale(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ prenotazioni }) => {
      this.prenotazioni = prenotazioni;
      if (prenotazioni) {
        this.updateForm(prenotazioni);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const prenotazioni = this.prenotazioniFormService.getPrenotazioni(this.editForm);
    if (prenotazioni.id !== null) {
      this.subscribeToSaveResponse(this.prenotazioniService.update(prenotazioni));
    } else {
      this.subscribeToSaveResponse(this.prenotazioniService.create(prenotazioni));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPrenotazioni>>): void {
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

  protected updateForm(prenotazioni: IPrenotazioni): void {
    this.prenotazioni = prenotazioni;
    this.prenotazioniFormService.resetForm(this.editForm, prenotazioni);

    this.statiPrenotazionesSharedCollection = this.statiPrenotazioneService.addStatiPrenotazioneToCollectionIfMissing<IStatiPrenotazione>(
      this.statiPrenotazionesSharedCollection,
      prenotazioni.stato,
    );
    this.utentisSharedCollection = this.utentiService.addUtentiToCollectionIfMissing<IUtenti>(
      this.utentisSharedCollection,
      prenotazioni.utente,
    );
    this.salesSharedCollection = this.saleService.addSaleToCollectionIfMissing<ISale>(this.salesSharedCollection, prenotazioni.sala);
  }

  protected loadRelationshipsOptions(): void {
    this.statiPrenotazioneService
      .query()
      .pipe(map((res: HttpResponse<IStatiPrenotazione[]>) => res.body ?? []))
      .pipe(
        map((statiPrenotaziones: IStatiPrenotazione[]) =>
          this.statiPrenotazioneService.addStatiPrenotazioneToCollectionIfMissing<IStatiPrenotazione>(
            statiPrenotaziones,
            this.prenotazioni?.stato,
          ),
        ),
      )
      .subscribe((statiPrenotaziones: IStatiPrenotazione[]) => (this.statiPrenotazionesSharedCollection = statiPrenotaziones));

    this.utentiService
      .query()
      .pipe(map((res: HttpResponse<IUtenti[]>) => res.body ?? []))
      .pipe(map((utentis: IUtenti[]) => this.utentiService.addUtentiToCollectionIfMissing<IUtenti>(utentis, this.prenotazioni?.utente)))
      .subscribe((utentis: IUtenti[]) => (this.utentisSharedCollection = utentis));

    this.saleService
      .query()
      .pipe(map((res: HttpResponse<ISale[]>) => res.body ?? []))
      .pipe(map((sales: ISale[]) => this.saleService.addSaleToCollectionIfMissing<ISale>(sales, this.prenotazioni?.sala)))
      .subscribe((sales: ISale[]) => (this.salesSharedCollection = sales));
  }
}
