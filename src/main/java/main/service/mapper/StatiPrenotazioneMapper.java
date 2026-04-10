package main.service.mapper;

import main.domain.StatiPrenotazione;
import main.service.dto.StatiPrenotazioneDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link StatiPrenotazione} and its DTO {@link StatiPrenotazioneDTO}.
 */
@Mapper(componentModel = "spring")
public interface StatiPrenotazioneMapper extends EntityMapper<StatiPrenotazioneDTO, StatiPrenotazione> {}
