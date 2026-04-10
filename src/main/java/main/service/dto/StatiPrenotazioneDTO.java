package main.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import main.domain.enumeration.StatoCodice;

/**
 * A DTO for the {@link main.domain.StatiPrenotazione} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class StatiPrenotazioneDTO implements Serializable {

    private UUID id;

    @NotNull
    private String descrizione;

    @NotNull
    private StatoCodice codice;

    @NotNull
    private Integer ordineAzione;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public StatoCodice getCodice() {
        return codice;
    }

    public void setCodice(StatoCodice codice) {
        this.codice = codice;
    }

    public Integer getOrdineAzione() {
        return ordineAzione;
    }

    public void setOrdineAzione(Integer ordineAzione) {
        this.ordineAzione = ordineAzione;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StatiPrenotazioneDTO)) {
            return false;
        }

        StatiPrenotazioneDTO statiPrenotazioneDTO = (StatiPrenotazioneDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, statiPrenotazioneDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "StatiPrenotazioneDTO{" +
            "id='" + getId() + "'" +
            ", descrizione='" + getDescrizione() + "'" +
            ", codice='" + getCodice() + "'" +
            ", ordineAzione=" + getOrdineAzione() +
            "}";
    }
}
