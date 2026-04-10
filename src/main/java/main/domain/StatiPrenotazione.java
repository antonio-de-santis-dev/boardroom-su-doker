package main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.UUID;
import main.domain.enumeration.StatoCodice;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A StatiPrenotazione.
 */
@Entity
@Table(name = "stati_prenotazione")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StatiPrenotazione implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @NotNull
    @Column(name = "descrizione", nullable = false)
    private String descrizione;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "codice", nullable = false)
    private StatoCodice codice;

    @NotNull
    @Column(name = "ordine_azione", nullable = false)
    private Integer ordineAzione;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public StatiPrenotazione id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescrizione() {
        return this.descrizione;
    }

    public StatiPrenotazione descrizione(String descrizione) {
        this.setDescrizione(descrizione);
        return this;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public StatoCodice getCodice() {
        return this.codice;
    }

    public StatiPrenotazione codice(StatoCodice codice) {
        this.setCodice(codice);
        return this;
    }

    public void setCodice(StatoCodice codice) {
        this.codice = codice;
    }

    public Integer getOrdineAzione() {
        return this.ordineAzione;
    }

    public StatiPrenotazione ordineAzione(Integer ordineAzione) {
        this.setOrdineAzione(ordineAzione);
        return this;
    }

    public void setOrdineAzione(Integer ordineAzione) {
        this.ordineAzione = ordineAzione;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StatiPrenotazione)) {
            return false;
        }
        return getId() != null && getId().equals(((StatiPrenotazione) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "StatiPrenotazione{" +
            "id=" +
            getId() +
            ", descrizione='" +
            getDescrizione() +
            "'" +
            ", codice='" +
            getCodice() +
            "'" +
            ", ordineAzione=" +
            getOrdineAzione() +
            "}"
        );
    }
}
