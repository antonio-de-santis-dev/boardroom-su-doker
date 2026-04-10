package main.domain;

import static main.domain.UtentiTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UtentiTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Utenti.class);
        Utenti utenti1 = getUtentiSample1();
        Utenti utenti2 = new Utenti();
        assertThat(utenti1).isNotEqualTo(utenti2);

        utenti2.setId(utenti1.getId());
        assertThat(utenti1).isEqualTo(utenti2);

        utenti2 = getUtentiSample2();
        assertThat(utenti1).isNotEqualTo(utenti2);
    }
}
