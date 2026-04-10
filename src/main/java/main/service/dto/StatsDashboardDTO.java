package main.service.dto;

import java.util.List;

/**
 * DTO contenente tutte le statistiche aggregate per la dashboard admin.
 * Un singolo endpoint ritorna tutto per minimizzare le chiamate HTTP.
 */
public class StatsDashboardDTO {

    /** Prenotazioni totali nel periodo */
    private long totalePrenotazioni;

    /** Prenotazioni confermate nel periodo */
    private long prenotazioniConfermate;

    /** Tasso di occupazione medio (%) */
    private double tassoOccupazionePercentuale;

    /** Utenti unici che hanno prenotato */
    private long utentiAttivi;

    // ── Grafico 1: Prenotazioni per sala (bar) ────────────────
    /** [ {nome: "Sala A", count: 42}, ... ] */
    private List<LabelValueDTO> prenotazioniPerSala;

    // ── Grafico 2: Ore più richieste (line/bar) ───────────────
    /** [ {label: "08:00", count: 15}, ..., {label: "19:00", count: 3} ] */
    private List<LabelValueDTO> oreRichieste;

    // ── Grafico 3: Tasso occupazione per mese (line) ──────────
    /** [ {label: "Gen 2026", value: 68.5}, ... ] */
    private List<LabelDoubleDTO> occupazionePerMese;

    // ── Grafico 4: Top 5 utenti più attivi (bar) ──────────────
    /** [ {label: "Mario Rossi", count: 12}, ... ] */
    private List<LabelValueDTO> topUtenti;

    // ── Grafico 5: Prenotazioni per giorno settimana (bar) ────
    /** [ {label: "Lunedì", count: 28}, ..., {label: "Domenica", count: 2} ] */
    private List<LabelValueDTO> prenotazioniPerGiorno;

    // ── Nested DTOs ───────────────────────────────────────────

    public static class LabelValueDTO {

        private String label;
        private long value;

        public LabelValueDTO(String label, long value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class LabelDoubleDTO {

        private String label;
        private double value;

        public LabelDoubleDTO(String label, double value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    // ── Getters & Setters ─────────────────────────────────────

    public long getTotalePrenotazioni() {
        return totalePrenotazioni;
    }

    public void setTotalePrenotazioni(long v) {
        this.totalePrenotazioni = v;
    }

    public long getPrenotazioniConfermate() {
        return prenotazioniConfermate;
    }

    public void setPrenotazioniConfermate(long v) {
        this.prenotazioniConfermate = v;
    }

    public double getTassoOccupazionePercentuale() {
        return tassoOccupazionePercentuale;
    }

    public void setTassoOccupazionePercentuale(double v) {
        this.tassoOccupazionePercentuale = v;
    }

    public long getUtentiAttivi() {
        return utentiAttivi;
    }

    public void setUtentiAttivi(long v) {
        this.utentiAttivi = v;
    }

    public List<LabelValueDTO> getPrenotazioniPerSala() {
        return prenotazioniPerSala;
    }

    public void setPrenotazioniPerSala(List<LabelValueDTO> v) {
        this.prenotazioniPerSala = v;
    }

    public List<LabelValueDTO> getOreRichieste() {
        return oreRichieste;
    }

    public void setOreRichieste(List<LabelValueDTO> v) {
        this.oreRichieste = v;
    }

    public List<LabelDoubleDTO> getOccupazionePerMese() {
        return occupazionePerMese;
    }

    public void setOccupazionePerMese(List<LabelDoubleDTO> v) {
        this.occupazionePerMese = v;
    }

    public List<LabelValueDTO> getTopUtenti() {
        return topUtenti;
    }

    public void setTopUtenti(List<LabelValueDTO> v) {
        this.topUtenti = v;
    }

    public List<LabelValueDTO> getPrenotazioniPerGiorno() {
        return prenotazioniPerGiorno;
    }

    public void setPrenotazioniPerGiorno(List<LabelValueDTO> v) {
        this.prenotazioniPerGiorno = v;
    }
}
