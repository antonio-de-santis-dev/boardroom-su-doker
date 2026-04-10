package main.service.mapper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.service.dto.EventiDTO;
import main.service.dto.PrenotazioniDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Eventi} and its DTO {@link EventiDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventiMapper extends EntityMapper<EventiDTO, Eventi> {
    @Mapping(target = "prenotazioneId", source = "prenotazione.id")
    @Mapping(source = "prenotazione.data", target = "data")
    @Mapping(source = "prenotazione.oraInizio", target = "oraInizio")
    @Mapping(source = "prenotazione.oraFine", target = "oraFine")
    @Mapping(source = "prenotazione.sala.nome", target = "salaNome")
    @Mapping(source = "prenotazione.sala.imageUrl", target = "salaImageUrl")
    @Mapping(source = "locandinaUrl", target = "locandinaUrl")
    @Mapping(source = "descrizione", target = "descrizione")
    @Mapping(source = "prenotazione.numPersone", target = "numPersone")
    @Mapping(target = "postiOccupati", ignore = true)
    @Mapping(target = "eventoPieno", ignore = true)
    EventiDTO toDto(Eventi s);

    List<EventiDTO> toDto(List<Eventi> eventiList);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
