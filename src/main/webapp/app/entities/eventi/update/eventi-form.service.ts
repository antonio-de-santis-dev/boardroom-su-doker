import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IEventi, NewEventi } from '../eventi.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IEventi for edit and NewEventiFormGroupInput for create.
 */
type EventiFormGroupInput = IEventi | PartialWithRequiredKeyOf<NewEventi>;

type EventiFormDefaults = Pick<NewEventi, 'id'>;

type EventiFormGroupContent = {
  id: FormControl<IEventi['id'] | NewEventi['id']>;
  titolo: FormControl<IEventi['titolo']>;
  descrizione: FormControl<IEventi['descrizione']>;
  prezzo: FormControl<IEventi['prezzo']>;
};

export type EventiFormGroup = FormGroup<EventiFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class EventiFormService {
  createEventiFormGroup(eventi: EventiFormGroupInput = { id: null }): EventiFormGroup {
    const eventiRawValue = {
      ...this.getFormDefaults(),
      ...eventi,
    };
    return new FormGroup<EventiFormGroupContent>({
      id: new FormControl(
        { value: eventiRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      titolo: new FormControl(eventiRawValue.titolo, {
        validators: [Validators.required],
      }),
      descrizione: new FormControl(eventiRawValue.descrizione),
      prezzo: new FormControl(eventiRawValue.prezzo),
    });
  }

  getEventi(form: EventiFormGroup): IEventi | NewEventi {
    return form.getRawValue() as IEventi | NewEventi;
  }

  resetForm(form: EventiFormGroup, eventi: EventiFormGroupInput): void {
    const eventiRawValue = { ...this.getFormDefaults(), ...eventi };
    form.reset({
      ...eventiRawValue,
      id: { value: eventiRawValue.id, disabled: true },
    } as any);
  }

  private getFormDefaults(): EventiFormDefaults {
    return {
      id: null,
    };
  }
}
