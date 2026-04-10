export interface ISale {
  id: string;
  nome?: string | null;
  capienza?: number | null;
  descrizione?: string | null;
}

export type NewSale = Omit<ISale, 'id'> & { id: null };
