package main.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class StatiPrenotazioneDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(StatiPrenotazioneDTO.class);
        StatiPrenotazioneDTO statiPrenotazioneDTO1 = new StatiPrenotazioneDTO();
        statiPrenotazioneDTO1.setId(UUID.randomUUID());
        StatiPrenotazioneDTO statiPrenotazioneDTO2 = new StatiPrenotazioneDTO();
        assertThat(statiPrenotazioneDTO1).isNotEqualTo(statiPrenotazioneDTO2);
        statiPrenotazioneDTO2.setId(statiPrenotazioneDTO1.getId());
        assertThat(statiPrenotazioneDTO1).isEqualTo(statiPrenotazioneDTO2);
        statiPrenotazioneDTO2.setId(UUID.randomUUID());
        assertThat(statiPrenotazioneDTO1).isNotEqualTo(statiPrenotazioneDTO2);
        statiPrenotazioneDTO1.setId(null);
        assertThat(statiPrenotazioneDTO1).isNotEqualTo(statiPrenotazioneDTO2);
    }
}
