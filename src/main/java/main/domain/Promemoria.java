package main.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import main.domain.enumeration.TipoPromemoria;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Configurazione promemoria per una singola prenotazione.
 * Un record = un tipo di promemoria attivato.
 *
 * Esempio: utente attiva "1 giorno prima" e "giorno stesso"
 * → 2 record Promemoria per la stessa prenotazione.
 */
@Entity
@Table(
    name = "promemoria",
    uniqueConstraints = @UniqueConstraint(name = "uq_promemoria_prenotazione_tipo", columnNames = { "prenotazione_id", "tipo" })
)
@EntityListeners(AuditingEntityListener.class)
public class Promemoria implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    /** Prenotazione a cui si riferisce il promemoria */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prenotazione_id", nullable = false)
    @JsonIgnoreProperties(value = { "promemoria" }, allowSetters = true)
    private Prenotazioni prenotazione;

    /** Tipo di promemoria (WEEK_BEFORE, TWO_DAYS_BEFORE, DAY_BEFORE, SAME_DAY) */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoPromemoria tipo;

    /** true = promemoria attivo, false = disattivato dall'utente */
    @Column(name = "abilitato", nullable = false)
    private boolean abilitato = true;

    /**
     * true = email già inviata.
     * Campo chiave per la strategia anti-duplicati:
     * lo scheduler imposta questo a true dopo l'invio e non riprocessa il record.
     */
    @Column(name = "inviato", nullable = false)
    private boolean inviato = false;

    /** Timestamp dell'invio (null se non ancora inviato) — per audit/logging */
    @Column(name = "inviato_alle")
    private LocalDateTime inviatoAlle;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Getters & Setters ─────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Prenotazioni getPrenotazione() {
        return prenotazione;
    }

    public void setPrenotazione(Prenotazioni prenotazione) {
        this.prenotazione = prenotazione;
    }

    public TipoPromemoria getTipo() {
        return tipo;
    }

    public void setTipo(TipoPromemoria tipo) {
        this.tipo = tipo;
    }

    public boolean isAbilitato() {
        return abilitato;
    }

    public void setAbilitato(boolean abilitato) {
        this.abilitato = abilitato;
    }

    public boolean isInviato() {
        return inviato;
    }

    public void setInviato(boolean inviato) {
        this.inviato = inviato;
    }

    public LocalDateTime getInviatoAlle() {
        return inviatoAlle;
    }

    public void setInviatoAlle(LocalDateTime inviatoAlle) {
        this.inviatoAlle = inviatoAlle;
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
        if (!(o instanceof Promemoria)) return false;
        return id != null && id.equals(((Promemoria) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
