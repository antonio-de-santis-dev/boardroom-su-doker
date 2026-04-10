package main.domain;

import static main.domain.StatiPrenotazioneTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class StatiPrenotazioneTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StatiPrenotazione.class);
        StatiPrenotazione statiPrenotazione1 = getStatiPrenotazioneSample1();
        StatiPrenotazione statiPrenotazione2 = new StatiPrenotazione();
        assertThat(statiPrenotazione1).isNotEqualTo(statiPrenotazione2);

        statiPrenotazione2.setId(statiPrenotazione1.getId());
        assertThat(statiPrenotazione1).isEqualTo(statiPrenotazione2);

        statiPrenotazione2 = getStatiPrenotazioneSample2();
        assertThat(statiPrenotazione1).isNotEqualTo(statiPrenotazione2);
    }
}
