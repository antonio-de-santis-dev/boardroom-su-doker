import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { EventiService } from '../service/eventi.service';
import { IEventi } from '../eventi.model';
import { EventiFormGroup, EventiFormService } from './eventi-form.service';

@Component({
  selector: 'jhi-eventi-update',
  templateUrl: './eventi-update.component.html',
  styleUrl: './eventi-update.component.scss',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class EventiUpdateComponent implements OnInit {
  isSaving = false;
  eventi: IEventi | null = null;

  protected eventiService = inject(EventiService);
  protected eventiFormService = inject(EventiFormService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: EventiFormGroup = this.eventiFormService.createEventiFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ eventi }) => {
      this.eventi = eventi;
      if (eventi) {
        this.updateForm(eventi);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const eventi = this.eventiFormService.getEventi(this.editForm) as IEventi;
    this.subscribeToSaveResponse(this.eventiService.update(eventi));
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IEventi>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }
  protected onSaveError(): void {}
  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(eventi: IEventi): void {
    this.eventi = eventi;
    this.eventiFormService.resetForm(this.editForm, eventi);
  }
}
