package main.service.mapper;

import static main.domain.StatiPrenotazioneAsserts.*;
import static main.domain.StatiPrenotazioneTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatiPrenotazioneMapperTest {

    private StatiPrenotazioneMapper statiPrenotazioneMapper;

    @BeforeEach
    void setUp() {
        statiPrenotazioneMapper = new StatiPrenotazioneMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getStatiPrenotazioneSample1();
        var actual = statiPrenotazioneMapper.toEntity(statiPrenotazioneMapper.toDto(expected));
        assertStatiPrenotazioneAllPropertiesEquals(expected, actual);
    }
}
