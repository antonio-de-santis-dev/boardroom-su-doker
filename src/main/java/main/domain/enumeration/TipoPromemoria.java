package main.domain.enumeration;

/**
 * Tipi di promemoria supportati.
 * Il valore numerico rappresenta i giorni prima dell'evento.
 */
public enum TipoPromemoria {
    /** 7 giorni prima dell'evento */
    WEEK_BEFORE(7),

    /** 2 giorni prima dell'evento */
    TWO_DAYS_BEFORE(2),

    /** 1 giorno prima dell'evento */
    DAY_BEFORE(1),

    /** Stesso giorno dell'evento */
    SAME_DAY(0);

    private final int giorniPrima;

    TipoPromemoria(int giorniPrima) {
        this.giorniPrima = giorniPrima;
    }

    public int getGiorniPrima() {
        return giorniPrima;
    }

    /** Etichetta leggibile in italiano */
    public String getEtichetta() {
        return switch (this) {
            case WEEK_BEFORE -> "1 settimana prima";
            case TWO_DAYS_BEFORE -> "2 giorni prima";
            case DAY_BEFORE -> "1 giorno prima";
            case SAME_DAY -> "Il giorno stesso";
        };
    }
}
