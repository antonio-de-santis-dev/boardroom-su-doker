package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link main.domain.Sale} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SaleDTO implements Serializable {

    private UUID id;

    @NotNull
    private String nome;

    @NotNull
    private Integer capienza;

    private String descrizione;

    /** Path relativo dell'immagine. Null = nessuna immagine. */
    private String imageUrl;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCapienza() {
        return capienza;
    }

    public void setCapienza(Integer capienza) {
        this.capienza = capienza;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SaleDTO)) return false;
        SaleDTO saleDTO = (SaleDTO) o;
        if (this.id == null) return false;
        return Objects.equals(this.id, saleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return (
            "SaleDTO{" +
            "id='" +
            getId() +
            "'" +
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
