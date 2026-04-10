package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class PrenotazioniDTO implements Serializable {

    private UUID id;

    @NotNull
    private LocalDate data;

    @NotNull
    private LocalTime oraInizio;

    @NotNull
    private LocalTime oraFine;

    private Integer numPersone;

    private StatiPrenotazioneDTO stato;
    private UtentiDTO utente;
    private SaleDTO sala;
    private EventiDTO evento;

    private UUID salaId;
    private UUID eventoId;
    private String codiceQr;

    /**
     * ID della serie ricorrente di appartenenza.
     * null se la prenotazione è singola.
     * Usato nel frontend per:
     *  - mostrare il badge "Ricorrente" nella lista
     *  - collegare la card della serie nella sezione ricorrenze
     */
    private UUID ricorrenzaId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
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

    public StatiPrenotazioneDTO getStato() {
        return stato;
    }

    public void setStato(StatiPrenotazioneDTO stato) {
        this.stato = stato;
    }

    public UtentiDTO getUtente() {
        return utente;
    }

    public void setUtente(UtentiDTO utente) {
        this.utente = utente;
    }

    public SaleDTO getSala() {
        return sala;
    }

    public void setSala(SaleDTO sala) {
        this.sala = sala;
    }

    public Integer getNumPersone() {
        return numPersone;
    }

    public void setNumPersone(Integer numPersone) {
        this.numPersone = numPersone;
    }

    public UUID getSalaId() {
        return salaId;
    }

    public void setSalaId(UUID salaId) {
        this.salaId = salaId;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public EventiDTO getEvento() {
        return evento;
    }

    public void setEvento(EventiDTO evento) {
        this.evento = evento;
    }

    public String getCodiceQr() {
        return codiceQr;
    }

    public void setCodiceQr(String codiceQr) {
        this.codiceQr = codiceQr;
    }

    public UUID getRicorrenzaId() {
        return ricorrenzaId;
    }

    public void setRicorrenzaId(UUID ricorrenzaId) {
        this.ricorrenzaId = ricorrenzaId;
    }
}
