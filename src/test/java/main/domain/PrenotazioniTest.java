package main.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrenotazioniTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prenotazioni.class);
        Prenotazioni prenotazione1 = new Prenotazioni();
        prenotazione1.setId(UUID.randomUUID());
        Prenotazioni prenotazione2 = new Prenotazioni();
        prenotazione2.setId(prenotazione1.getId());
        assertThat(prenotazione1).isEqualTo(prenotazione2);

        prenotazione2.setId(UUID.randomUUID());
        assertThat(prenotazione1).isNotEqualTo(prenotazione2);

        prenotazione1.setId(null);
        assertThat(prenotazione1).isNotEqualTo(prenotazione2);
    }

    @Test
    void basicFields_shouldBeSetAndRetrievedCorrectly() {
        Prenotazioni prenotazione = new Prenotazioni();
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime oraInizio = LocalTime.of(10, 0);
        LocalTime oraFine = LocalTime.of(11, 0);

        prenotazione.setData(data);
        prenotazione.setOraInizio(oraInizio);
        prenotazione.setOraFine(oraFine);

        assertThat(prenotazione.getData()).isEqualTo(data);
        assertThat(prenotazione.getOraInizio()).isEqualTo(oraInizio);
        assertThat(prenotazione.getOraFine()).isEqualTo(oraFine);
    }
}
