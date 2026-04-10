package main.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity class for Prenotazioni
 */
@Entity
@Table(name = "prenotazioni")
@EntityListeners(AuditingEntityListener.class)
public class Prenotazioni implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @NotNull
    @Column(name = "data", nullable = false)
    private LocalDate data;

    @NotNull
    @Column(name = "ora_inizio", nullable = false)
    private LocalTime oraInizio;

    @NotNull
    @Column(name = "ora_fine", nullable = false)
    private LocalTime oraFine;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "prenotazione")
    @JsonIgnoreProperties(value = { "prenotazione" }, allowSetters = true)
    private Eventi evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private StatiPrenotazione stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private Utenti utente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private Sale sala;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "codice_qr", unique = true)
    private String codiceQr;

    @Column(name = "num_persone")
    private Integer numPersone;

    /** Posizione nella waitlist (null = non in waitlist). 1 = primo in coda. */
    @Column(name = "posizione_waitlist")
    private Integer posizioneWaitlist;

    /** Timestamp di quando la prenotazione e' stata promossa dalla waitlist. */
    @Column(name = "promossa_at")
    private java.time.LocalDateTime promossaAt;

    // ── RICORRENZA ────────────────────────────────────────────────────────────
    /**
     * Collegamento alla serie ricorrente di appartenenza.
     * Null se la prenotazione è singola (non ricorrente).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ricorrenza_id")
    @JsonIgnoreProperties(value = { "istanze" }, allowSetters = true)
    private Ricorrenza ricorrenza;

    // ─────────────────────────────────────────────────────────────────────────

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

    public Eventi getEvento() {
        return evento;
    }

    public void setEvento(Eventi evento) {
        this.evento = evento;
    }

    public StatiPrenotazione getStato() {
        return stato;
    }

    public void setStato(StatiPrenotazione stato) {
        this.stato = stato;
    }

    public Utenti getUtente() {
        return utente;
    }

    public void setUtente(Utenti utente) {
        this.utente = utente;
    }

    public Sale getSala() {
        return sala;
    }

    public void setSala(Sale sala) {
        this.sala = sala;
    }

    public Prenotazioni id(UUID id) {
        this.setId(id);
        return this;
    }

    public Prenotazioni data(LocalDate data) {
        this.setData(data);
        return this;
    }

    public Prenotazioni oraInizio(LocalTime oraInizio) {
        this.setOraInizio(oraInizio);
        return this;
    }

    public Prenotazioni oraFine(LocalTime oraFine) {
        this.setOraFine(oraFine);
        return this;
    }

    public Prenotazioni stato(StatiPrenotazione stato) {
        this.setStato(stato);
        return this;
    }

    public Prenotazioni utente(Utenti utente) {
        this.setUtente(utente);
        return this;
    }

    public Prenotazioni sala(Sale sala) {
        this.setSala(sala);
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCodiceQr() {
        return codiceQr;
    }

    public void setCodiceQr(String codiceQr) {
        this.codiceQr = codiceQr;
    }

    public Integer getNumPersone() {
        return numPersone;
    }

    public void setNumPersone(Integer numPersone) {
        this.numPersone = numPersone;
    }

    public Integer getPosizioneWaitlist() {
        return posizioneWaitlist;
    }

    public void setPosizioneWaitlist(Integer posizioneWaitlist) {
        this.posizioneWaitlist = posizioneWaitlist;
    }

    public java.time.LocalDateTime getPromossaAt() {
        return promossaAt;
    }

    public void setPromossaAt(java.time.LocalDateTime promossaAt) {
        this.promossaAt = promossaAt;
    }

    public Ricorrenza getRicorrenza() {
        return ricorrenza;
    }

    public void setRicorrenza(Ricorrenza ricorrenza) {
        this.ricorrenza = ricorrenza;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prenotazioni)) return false;
        return id != null && id.equals(((Prenotazioni) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
