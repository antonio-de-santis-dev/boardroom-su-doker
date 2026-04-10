package main.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import main.domain.enumeration.Frequenza;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Rappresenta la regola di ripetizione di una serie di prenotazioni.
 *
 * Relazione: una Ricorrenza genera N Prenotazioni.
 * Ogni prenotazione figlia conserva ricorrenza_id per sapere a quale serie appartiene.
 *
 * Strategia: EAGER — tutte le occorrenze vengono generate immediatamente alla creazione.
 */
@Entity
@Table(name = "ricorrenza")
@EntityListeners(AuditingEntityListener.class)
public class Ricorrenza implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    /** Frequenza della ripetizione: WEEKLY, BIWEEKLY, MONTHLY */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "frequenza", nullable = false, length = 20)
    private Frequenza frequenza;

    /**
     * Giorni della settimana attivi per WEEKLY/BIWEEKLY.
     * Salvati come stringa CSV: "MONDAY,WEDNESDAY,FRIDAY"
     */
    @Column(name = "giorni_settimana", length = 100)
    private String giorniSettimana;

    @NotNull
    @Column(name = "data_inizio", nullable = false)
    private LocalDate dataInizio;

    /** Data di fine della serie (esclusiva con numOccorrenze) */
    @Column(name = "data_fine")
    private LocalDate dataFine;

    /** Numero massimo di occorrenze (esclusivo con dataFine) */
    @Column(name = "num_occorrenze")
    private Integer numOccorrenze;

    @NotNull
    @Column(name = "ora_inizio", nullable = false)
    private LocalTime oraInizio;

    @NotNull
    @Column(name = "ora_fine", nullable = false)
    private LocalTime oraFine;

    @Column(name = "num_persone")
    private Integer numPersone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private Sale sala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_id", nullable = false)
    @JsonIgnoreProperties(value = { "prenotazionis" }, allowSetters = true)
    private Utenti utente;

    /** Le prenotazioni generate da questa regola */
    @OneToMany(mappedBy = "ricorrenza", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "ricorrenza" }, allowSetters = true)
    private List<Prenotazioni> istanze = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Getters & Setters ─────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Frequenza getFrequenza() {
        return frequenza;
    }

    public void setFrequenza(Frequenza frequenza) {
        this.frequenza = frequenza;
    }

    public String getGiorniSettimana() {
        return giorniSettimana;
    }

    public void setGiorniSettimana(String giorniSettimana) {
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

    public Sale getSala() {
        return sala;
    }

    public void setSala(Sale sala) {
        this.sala = sala;
    }

    public Utenti getUtente() {
        return utente;
    }

    public void setUtente(Utenti utente) {
        this.utente = utente;
    }

    public List<Prenotazioni> getIstanze() {
        return istanze;
    }

    public void setIstanze(List<Prenotazioni> istanze) {
        this.istanze = istanze;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ricorrenza)) return false;
        return id != null && id.equals(((Ricorrenza) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
