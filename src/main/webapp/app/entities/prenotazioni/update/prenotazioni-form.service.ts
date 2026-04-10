import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IPrenotazioni, NewPrenotazioni } from '../prenotazioni.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPrenotazioni for edit and NewPrenotazioniFormGroupInput for create.
 */
type PrenotazioniFormGroupInput = IPrenotazioni | PartialWithRequiredKeyOf<NewPrenotazioni>;

type PrenotazioniFormDefaults = Pick<NewPrenotazioni, 'id'>;

type PrenotazioniFormGroupContent = {
  id: FormControl<IPrenotazioni['id'] | NewPrenotazioni['id']>;
  data: FormControl<IPrenotazioni['data']>;
  oraInizio: FormControl<IPrenotazioni['oraInizio']>;
  oraFine: FormControl<IPrenotazioni['oraFine']>;
  stato: FormControl<IPrenotazioni['stato']>;
  utente: FormControl<IPrenotazioni['utente']>;
  sala: FormControl<IPrenotazioni['sala']>;
};

export type PrenotazioniFormGroup = FormGroup<PrenotazioniFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PrenotazioniFormService {
  createPrenotazioniFormGroup(prenotazioni: PrenotazioniFormGroupInput = { id: null }): PrenotazioniFormGroup {
    const prenotazioniRawValue = {
      ...this.getFormDefaults(),
      ...prenotazioni,
    };
    return new FormGroup<PrenotazioniFormGroupContent>({
      id: new FormControl(
        { value: prenotazioniRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      data: new FormControl(prenotazioniRawValue.data, {
        validators: [Validators.required],
      }),
      oraInizio: new FormControl(prenotazioniRawValue.oraInizio, {
        validators: [Validators.required],
      }),
      oraFine: new FormControl(prenotazioniRawValue.oraFine, {
        validators: [Validators.required],
      }),
      stato: new FormControl(prenotazioniRawValue.stato),
      utente: new FormControl(prenotazioniRawValue.utente),
      sala: new FormControl(prenotazioniRawValue.sala),
    });
  }

  getPrenotazioni(form: PrenotazioniFormGroup): IPrenotazioni | NewPrenotazioni {
    return form.getRawValue() as IPrenotazioni | NewPrenotazioni;
  }

  resetForm(form: PrenotazioniFormGroup, prenotazioni: PrenotazioniFormGroupInput): void {
    const prenotazioniRawValue = { ...this.getFormDefaults(), ...prenotazioni };
    form.reset(
      {
        ...prenotazioniRawValue,
        id: { value: prenotazioniRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PrenotazioniFormDefaults {
    return {
      id: null,
    };
  }
}
