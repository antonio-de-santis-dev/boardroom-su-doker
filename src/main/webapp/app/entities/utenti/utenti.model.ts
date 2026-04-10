import { IUser } from 'app/entities/user/user.model';

export interface IUtenti {
  id: string;
  nome?: string | null;
  nomeAzienda?: string | null;
  numeroDiTelefono?: string | null;
  user?: Pick<IUser, 'id'> | null;
}

export type NewUtenti = Omit<IUtenti, 'id'> & { id: null };
