package main.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.service.dto.PrenotazioniDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test per PrenotazioniMapper.
 *
 * PrenotazioniMapperImpl dipende da SaleMapper, UtentiMapper e StatiPrenotazioneMapper
 * (iniettati via @Mapper uses = {...}). Non può essere istanziato direttamente con new
 * senza i mapper dipendenti — viene mockato tramite @Mock + @InjectMocks.
 */
@ExtendWith(MockitoExtension.class)
class PrenotazioniMapperTest {

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private UtentiMapper utentiMapper;

    @Mock
    private StatiPrenotazioneMapper statiPrenotazioneMapper;

    @InjectMocks
    private PrenotazioniMapperImpl prenotazioniMapper;

    @Test
    void shouldConvertEntityToDto() {
        Prenotazioni entity = new Prenotazioni();
        entity.setId(UUID.randomUUID());
        entity.setData(LocalDate.now().plusDays(1));
        entity.setOraInizio(LocalTime.of(9, 0));
        entity.setOraFine(LocalTime.of(10, 0));

        PrenotazioniDTO dto = prenotazioniMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getData()).isEqualTo(entity.getData());
        assertThat(dto.getOraInizio()).isEqualTo(entity.getOraInizio());
        assertThat(dto.getOraFine()).isEqualTo(entity.getOraFine());
    }

    @Test
    void shouldConvertDtoToEntity() {
        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setId(UUID.randomUUID());
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(9, 0));
        dto.setOraFine(LocalTime.of(10, 0));

        Prenotazioni entity = prenotazioniMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getData()).isEqualTo(dto.getData());
    }

    @Test
    void shouldHandleNullEntity() {
        assertThat(prenotazioniMapper.toDto((Prenotazioni) null)).isNull();
    }

    @Test
    void shouldHandleNullDto() {
        assertThat(prenotazioniMapper.toEntity((PrenotazioniDTO) null)).isNull();
    }
}
