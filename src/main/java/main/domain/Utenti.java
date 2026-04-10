package main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A Utenti.
 */
@Entity
@Table(name = "utenti")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Utenti implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @NotNull
    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "nome_azienda")
    private String nomeAzienda;

    @NotNull
    @Column(name = "numero_di_telefono", nullable = false)
    private String numeroDiTelefono;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Utenti id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public Utenti nome(String nome) {
        this.setNome(nome);
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeAzienda() {
        return this.nomeAzienda;
    }

    public Utenti nomeAzienda(String nomeAzienda) {
        this.setNomeAzienda(nomeAzienda);
        return this;
    }

    public void setNomeAzienda(String nomeAzienda) {
        this.nomeAzienda = nomeAzienda;
    }

    public String getNumeroDiTelefono() {
        return this.numeroDiTelefono;
    }

    public Utenti numeroDiTelefono(String numeroDiTelefono) {
        this.setNumeroDiTelefono(numeroDiTelefono);
        return this;
    }

    public void setNumeroDiTelefono(String numeroDiTelefono) {
        this.numeroDiTelefono = numeroDiTelefono;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Utenti user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Utenti)) {
            return false;
        }
        return getId() != null && getId().equals(((Utenti) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Utenti{" +
            "id=" + getId() +
            ", nome='" + getNome() + "'" +
            ", nomeAzienda='" + getNomeAzienda() + "'" +
            ", numeroDiTelefono='" + getNumeroDiTelefono() + "'" +
            "}";
    }
}
