package main.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import main.domain.enumeration.TipoEvento;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A Eventi.
 */
@Entity
@Table(name = "eventi")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Eventi implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @NotNull
    @Column(name = "titolo", nullable = false)
    private String titolo;

    @Column(name = "descrizione", nullable = false)
    private String descrizione;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoEvento tipo;

    @Column(name = "prezzo", precision = 21, scale = 2)
    private BigDecimal prezzo;

    /** Locandina dell'evento caricata dall'utente (base64 data URL). Null = usa immagine sala. */
    @Column(name = "locandina_url", columnDefinition = "LONGTEXT")
    private String locandinaUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prenotazione_id")
    @JsonIgnoreProperties(value = { "evento", "stato", "utente", "sala" }, allowSetters = true)
    private Prenotazioni prenotazione;

    public UUID getId() {
        return this.id;
    }

    public Eventi id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitolo() {
        return this.titolo;
    }

    public Eventi titolo(String titolo) {
        this.setTitolo(titolo);
        return this;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public TipoEvento getTipo() {
        return this.tipo;
    }

    public Eventi tipo(TipoEvento tipo) {
        this.setTipo(tipo);
        return this;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrezzo() {
        return this.prezzo;
    }

    public Eventi prezzo(BigDecimal prezzo) {
        this.setPrezzo(prezzo);
        return this;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public Prenotazioni getPrenotazione() {
        return this.prenotazione;
    }

    public void setPrenotazione(Prenotazioni prenotazioni) {
        this.prenotazione = prenotazioni;
    }

    public Eventi prenotazione(Prenotazioni prenotazioni) {
        this.setPrenotazione(prenotazioni);
        return this;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getLocandinaUrl() {
        return locandinaUrl;
    }

    public void setLocandinaUrl(String locandinaUrl) {
        this.locandinaUrl = locandinaUrl;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Eventi)) {
            return false;
        }
        return getId() != null && getId().equals(((Eventi) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Eventi{" +
            "id=" + getId() +
            ", titolo='" + getTitolo() + "'" +
            ", tipo='" + getTipo() + "'" +
            ", prezzo=" + getPrezzo() +
            "}";
    }
}
