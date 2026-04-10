import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IUtenti, NewUtenti } from '../utenti.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IUtenti for edit and NewUtentiFormGroupInput for create.
 */
type UtentiFormGroupInput = IUtenti | PartialWithRequiredKeyOf<NewUtenti>;

type UtentiFormDefaults = Pick<NewUtenti, 'id'>;

type UtentiFormGroupContent = {
  id: FormControl<IUtenti['id'] | NewUtenti['id']>;
  nome: FormControl<IUtenti['nome']>;
  nomeAzienda: FormControl<IUtenti['nomeAzienda']>;
  numeroDiTelefono: FormControl<IUtenti['numeroDiTelefono']>;
  user: FormControl<IUtenti['user']>;
};

export type UtentiFormGroup = FormGroup<UtentiFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class UtentiFormService {
  createUtentiFormGroup(utenti: UtentiFormGroupInput = { id: null }): UtentiFormGroup {
    const utentiRawValue = {
      ...this.getFormDefaults(),
      ...utenti,
    };
    return new FormGroup<UtentiFormGroupContent>({
      id: new FormControl(
        { value: utentiRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      nome: new FormControl(utentiRawValue.nome, {
        validators: [Validators.required],
      }),
      nomeAzienda: new FormControl(utentiRawValue.nomeAzienda),
      numeroDiTelefono: new FormControl(utentiRawValue.numeroDiTelefono, {
        validators: [Validators.required],
      }),
      user: new FormControl(utentiRawValue.user),
    });
  }

  getUtenti(form: UtentiFormGroup): IUtenti | NewUtenti {
    return form.getRawValue() as IUtenti | NewUtenti;
  }

  resetForm(form: UtentiFormGroup, utenti: UtentiFormGroupInput): void {
    const utentiRawValue = { ...this.getFormDefaults(), ...utenti };
    form.reset(
      {
        ...utentiRawValue,
        id: { value: utentiRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): UtentiFormDefaults {
    return {
      id: null,
    };
  }
}
