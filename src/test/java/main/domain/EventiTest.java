package main.domain;

import static main.domain.EventiTestSamples.*;
import static main.domain.PrenotazioniTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventiTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Eventi.class);
        Eventi eventi1 = getEventiSample1();
        Eventi eventi2 = new Eventi();
        assertThat(eventi1).isNotEqualTo(eventi2);

        eventi2.setId(eventi1.getId());
        assertThat(eventi1).isEqualTo(eventi2);

        eventi2 = getEventiSample2();
        assertThat(eventi1).isNotEqualTo(eventi2);
    }

    @Test
    void prenotazioneTest() {
        Eventi eventi = getEventiRandomSampleGenerator();
        Prenotazioni prenotazioniBack = getPrenotazioniRandomSampleGenerator();

        eventi.setPrenotazione(prenotazioniBack);
        assertThat(eventi.getPrenotazione()).isEqualTo(prenotazioniBack);

        eventi.prenotazione(null);
        assertThat(eventi.getPrenotazione()).isNull();
    }
}
