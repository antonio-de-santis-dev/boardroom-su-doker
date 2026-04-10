package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;
import main.domain.enumeration.TipoEvento;

/**
 * A DTO for the {@link main.domain.Eventi} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventiDTO implements Serializable {

    private UUID id;

    @NotNull
    private String titolo;

    private TipoEvento tipo;

    private BigDecimal prezzo;
    private String descrizione;
    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private String salaNome;

    /** URL immagine della sala (es: /uploads/sale/uuid.jpg). Null = nessuna foto. */
    private String salaImageUrl;

    /** Locandina dell'evento (base64 data URL). Se presente sostituisce l'immagine sala. */
    private String locandinaUrl;

    private UUID prenotazioneId;

    /** Numero massimo di partecipanti ammessi all'evento pubblico. */
    private Integer numPersone;

    /** Numero di posti già prenotati (calcolato a runtime, non nel DB). */
    private long postiOccupati;

    /** true se l'evento ha raggiunto la capienza massima. */
    private boolean eventoPieno;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public UUID getPrenotazioneId() {
        return prenotazioneId;
    }

    public void setPrenotazioneId(UUID prenotazioneId) {
        this.prenotazioneId = prenotazioneId;
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

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    public String getSalaImageUrl() {
        return salaImageUrl;
    }

    public void setSalaImageUrl(String salaImageUrl) {
        this.salaImageUrl = salaImageUrl;
    }

    public String getLocandinaUrl() {
        return locandinaUrl;
    }

    public void setLocandinaUrl(String locandinaUrl) {
        this.locandinaUrl = locandinaUrl;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Integer getNumPersone() {
        return numPersone;
    }

    public void setNumPersone(Integer numPersone) {
        this.numPersone = numPersone;
    }

    public long getPostiOccupati() {
        return postiOccupati;
    }

    public void setPostiOccupati(long postiOccupati) {
        this.postiOccupati = postiOccupati;
    }

    public boolean isEventoPieno() {
        return eventoPieno;
    }

    public void setEventoPieno(boolean eventoPieno) {
        this.eventoPieno = eventoPieno;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventiDTO)) {
            return false;
        }

        EventiDTO eventiDTO = (EventiDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, eventiDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventiDTO{" +
            "id='" + getId() + "'" +
            ", titolo='" + getTitolo() + "'" +
            ", tipo='" + getTipo() + "'" +
            ", prezzo=" + getPrezzo() +
            ", prenotazione=" + getPrenotazioneId() +
            "}";
    }
}
