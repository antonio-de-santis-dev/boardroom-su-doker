package main.domain.enumeration;

/**
 * Frequenza di ripetizione di una prenotazione ricorrente.
 */
public enum Frequenza {
    /** Ogni settimana nei giorni specificati */
    WEEKLY,

    /** Ogni due settimane nei giorni specificati */
    BIWEEKLY,

    /** Ogni mese, stesso giorno del mese della data di inizio */
    MONTHLY,
}
