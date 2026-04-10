import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IStatiPrenotazione, NewStatiPrenotazione } from '../stati-prenotazione.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IStatiPrenotazione for edit and NewStatiPrenotazioneFormGroupInput for create.
 */
type StatiPrenotazioneFormGroupInput = IStatiPrenotazione | PartialWithRequiredKeyOf<NewStatiPrenotazione>;

type StatiPrenotazioneFormDefaults = Pick<NewStatiPrenotazione, 'id'>;

type StatiPrenotazioneFormGroupContent = {
  id: FormControl<IStatiPrenotazione['id'] | NewStatiPrenotazione['id']>;
  descrizione: FormControl<IStatiPrenotazione['descrizione']>;
  codice: FormControl<IStatiPrenotazione['codice']>;
  ordineAzione: FormControl<IStatiPrenotazione['ordineAzione']>;
};

export type StatiPrenotazioneFormGroup = FormGroup<StatiPrenotazioneFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class StatiPrenotazioneFormService {
  createStatiPrenotazioneFormGroup(statiPrenotazione: StatiPrenotazioneFormGroupInput = { id: null }): StatiPrenotazioneFormGroup {
    const statiPrenotazioneRawValue = {
      ...this.getFormDefaults(),
      ...statiPrenotazione,
    };
    return new FormGroup<StatiPrenotazioneFormGroupContent>({
      id: new FormControl(
        { value: statiPrenotazioneRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      descrizione: new FormControl(statiPrenotazioneRawValue.descrizione, {
        validators: [Validators.required],
      }),
      codice: new FormControl(statiPrenotazioneRawValue.codice, {
        validators: [Validators.required],
      }),
      ordineAzione: new FormControl(statiPrenotazioneRawValue.ordineAzione, {
        validators: [Validators.required],
      }),
    });
  }

  getStatiPrenotazione(form: StatiPrenotazioneFormGroup): IStatiPrenotazione | NewStatiPrenotazione {
    return form.getRawValue() as IStatiPrenotazione | NewStatiPrenotazione;
  }

  resetForm(form: StatiPrenotazioneFormGroup, statiPrenotazione: StatiPrenotazioneFormGroupInput): void {
    const statiPrenotazioneRawValue = { ...this.getFormDefaults(), ...statiPrenotazione };
    form.reset(
      {
        ...statiPrenotazioneRawValue,
        id: { value: statiPrenotazioneRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): StatiPrenotazioneFormDefaults {
    return {
      id: null,
    };
  }
}
