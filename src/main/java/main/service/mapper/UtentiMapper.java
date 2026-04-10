package main.service.mapper;

import main.domain.Utenti;
import main.service.dto.UtentiDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UtentiMapper extends EntityMapper<UtentiDTO, Utenti> {}
