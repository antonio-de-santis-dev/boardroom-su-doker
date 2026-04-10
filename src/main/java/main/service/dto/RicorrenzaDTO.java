package main.service.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.domain.enumeration.Frequenza;

/**
 * DTO per la creazione e lettura di una regola di ricorrenza.
 *
 * Esempio payload creazione:
 * {
 *   "salaId": "uuid-sala",
 *   "frequenza": "WEEKLY",
 *   "giorniSettimana": ["MONDAY", "WEDNESDAY"],
 *   "dataInizio": "2026-04-07",
 *   "dataFine": "2026-06-30",
 *   "oraInizio": "09:00",
 *   "oraFine": "10:00",
 *   "numPersone": 5,
 *   "titoloEvento": "Stand-up mattutino",
 *   "descrizioneEvento": "Riunione ricorrente del team"
 * }
 */
public class RicorrenzaDTO implements Serializable {

    private UUID id;

    @NotNull
    private UUID salaId;

    private String salaNome;

    @NotNull
    private Frequenza frequenza;

    /**
     * Giorni selezionati per WEEKLY/BIWEEKLY.
     * Valori: "MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"
     */
    private List<String> giorniSettimana;

    @NotNull
    private LocalDate dataInizio;

    /** Data fine serie (obbligatoria se numOccorrenze è null) */
    private LocalDate dataFine;

    /** Numero occorrenze (obbligatorio se dataFine è null) */
    private Integer numOccorrenze;

    @NotNull
    private LocalTime oraInizio;

    @NotNull
    private LocalTime oraFine;

    private Integer numPersone;

    // ── Dati evento da propagare a tutte le istanze ───────────
    /**
     * Titolo dell'evento da associare a OGNI prenotazione della serie.
     * Viene usato per creare un evento separato per ogni istanza generata.
     */
    private String titoloEvento;

    /**
     * Descrizione dell'evento da propagare a tutte le istanze.
     */
    private String descrizioneEvento;

    /** Campi risposta (popolati dopo la creazione) */
    private int istanzeCreate;
    private int istanzeConflitto;
    private List<LocalDate> dateConflitto;

    // ── Getters & Setters ─────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSalaId() {
        return salaId;
    }

    public void setSalaId(UUID salaId) {
        this.salaId = salaId;
    }

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    public Frequenza getFrequenza() {
        return frequenza;
    }

    public void setFrequenza(Frequenza frequenza) {
        this.frequenza = frequenza;
    }

    public List<String> getGiorniSettimana() {
        return giorniSettimana;
    }

    public void setGiorniSettimana(List<String> giorniSettimana) {
        this.giorniSettimana = giorniSettimana;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDataFine(LocalDate dataFine) {
        this.dataFine = dataFine;
    }

    public Integer getNumOccorrenze() {
        return numOccorrenze;
    }

    public void setNumOccorrenze(Integer numOccorrenze) {
        this.numOccorrenze = numOccorrenze;
    }

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }

    public void setOraFine(LocalTime oraFine) {
        this.oraFine = oraFine;
    }

    public Integer getNumPersone() {
        return numPersone;
    }

    public void setNumPersone(Integer numPersone) {
        this.numPersone = numPersone;
    }

    public String getTitoloEvento() {
        return titoloEvento;
    }

    public void setTitoloEvento(String titoloEvento) {
        this.titoloEvento = titoloEvento;
    }

    public String getDescrizioneEvento() {
        return descrizioneEvento;
    }

    public void setDescrizioneEvento(String descrizioneEvento) {
        this.descrizioneEvento = descrizioneEvento;
    }

    public int getIstanzeCreate() {
        return istanzeCreate;
    }

    public void setIstanzeCreate(int istanzeCreate) {
        this.istanzeCreate = istanzeCreate;
    }

    public int getIstanzeConflitto() {
        return istanzeConflitto;
    }

    public void setIstanzeConflitto(int istanzeConflitto) {
        this.istanzeConflitto = istanzeConflitto;
    }

    public List<LocalDate> getDateConflitto() {
        return dateConflitto;
    }

    public void setDateConflitto(List<LocalDate> dateConflitto) {
        this.dateConflitto = dateConflitto;
    }
}
