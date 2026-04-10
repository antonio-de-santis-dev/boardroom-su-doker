import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUtenti } from '../utenti.model';
import { UtentiService } from '../service/utenti.service';
import { UtentiFormGroup, UtentiFormService } from './utenti-form.service';

@Component({
  selector: 'jhi-utenti-update',
  templateUrl: './utenti-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class UtentiUpdateComponent implements OnInit {
  isSaving = false;
  utenti: IUtenti | null = null;

  usersSharedCollection: IUser[] = [];

  protected utentiService = inject(UtentiService);
  protected utentiFormService = inject(UtentiFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: UtentiFormGroup = this.utentiFormService.createUtentiFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ utenti }) => {
      this.utenti = utenti;
      if (utenti) {
        this.updateForm(utenti);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const utenti = this.utentiFormService.getUtenti(this.editForm);
    if (utenti.id !== null) {
      this.subscribeToSaveResponse(this.utentiService.update(utenti));
    } else {
      this.subscribeToSaveResponse(this.utentiService.create(utenti));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IUtenti>>): void {
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

  protected updateForm(utenti: IUtenti): void {
    this.utenti = utenti;
    this.utentiFormService.resetForm(this.editForm, utenti);

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, utenti.user);
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.utenti?.user)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }
}
