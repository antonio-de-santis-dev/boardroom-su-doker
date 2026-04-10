package main.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A Sale.
 */
@Entity
@Table(name = "sale")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Sale implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @NotNull
    @Column(name = "nome", nullable = false)
    private String nome;

    @NotNull
    @Column(name = "capienza", nullable = false)
    private Integer capienza;

    @Column(name = "descrizione")
    private String descrizione;

    /**
     * Path relativo dell'immagine della sala sul server.
     * Esempio: "/uploads/sale/3f2504e0-sala.jpg"
     * Null = nessuna immagine caricata.
     */
    /** Immagine della sala come data URL Base64 (es: "data:image/jpeg;base64,..."). */
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sala")
    @JsonIgnoreProperties(value = { "eventis", "stato", "utente", "sala" }, allowSetters = true)
    private Set<Prenotazioni> prenotazionis = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Sale id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public Sale nome(String nome) {
        this.setNome(nome);
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCapienza() {
        return this.capienza;
    }

    public Sale capienza(Integer capienza) {
        this.setCapienza(capienza);
        return this;
    }

    public void setCapienza(Integer capienza) {
        this.capienza = capienza;
    }

    public String getDescrizione() {
        return this.descrizione;
    }

    public Sale descrizione(String descrizione) {
        this.setDescrizione(descrizione);
        return this;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public Sale imageUrl(String imageUrl) {
        this.setImageUrl(imageUrl);
        return this;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Set<Prenotazioni> getPrenotazionis() {
        return this.prenotazionis;
    }

    public void setPrenotazionis(Set<Prenotazioni> prenotazionis) {
        if (this.prenotazionis != null) {
            this.prenotazionis.forEach(i -> i.setSala(null));
        }
        if (prenotazionis != null) {
            prenotazionis.forEach(i -> i.setSala(this));
        }
        this.prenotazionis = prenotazionis;
    }

    public Sale prenotazionis(Set<Prenotazioni> prenotazionis) {
        this.setPrenotazionis(prenotazionis);
        return this;
    }

    public Sale addPrenotazioni(Prenotazioni prenotazioni) {
        this.prenotazionis.add(prenotazioni);
        prenotazioni.setSala(this);
        return this;
    }

    public Sale removePrenotazioni(Prenotazioni prenotazioni) {
        this.prenotazionis.remove(prenotazioni);
        prenotazioni.setSala(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sale)) return false;
        return getId() != null && getId().equals(((Sale) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Sale{" +
            "id=" +
            getId() +
            ", nome='" +
            getNome() +
            "'" +
            ", capienza=" +
            getCapienza() +
            ", descrizione='" +
            getDescrizione() +
            "'" +
            ", imageUrl='" +
            getImageUrl() +
            "'" +
            "}"
        );
    }
}
