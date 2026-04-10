package main.domain;

import static main.domain.PrenotazioniTestSamples.*;
import static main.domain.SaleTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import main.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SaleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Sale.class);
        Sale sale1 = getSaleSample1();
        Sale sale2 = new Sale();
        assertThat(sale1).isNotEqualTo(sale2);

        sale2.setId(sale1.getId());
        assertThat(sale1).isEqualTo(sale2);

        sale2 = getSaleSample2();
        assertThat(sale1).isNotEqualTo(sale2);
    }

    @Test
    void prenotazioniTest() {
        Sale sale = getSaleRandomSampleGenerator();
        Prenotazioni prenotazioniBack = getPrenotazioniRandomSampleGenerator();

        sale.addPrenotazioni(prenotazioniBack);
        assertThat(sale.getPrenotazionis()).containsOnly(prenotazioniBack);
        assertThat(prenotazioniBack.getSala()).isEqualTo(sale);

        sale.removePrenotazioni(prenotazioniBack);
        assertThat(sale.getPrenotazionis()).doesNotContain(prenotazioniBack);
        assertThat(prenotazioniBack.getSala()).isNull();

        sale.prenotazionis(new HashSet<>(Set.of(prenotazioniBack)));
        assertThat(sale.getPrenotazionis()).containsOnly(prenotazioniBack);
        assertThat(prenotazioniBack.getSala()).isEqualTo(sale);

        sale.setPrenotazionis(new HashSet<>());
        assertThat(sale.getPrenotazionis()).doesNotContain(prenotazioniBack);
        assertThat(prenotazioniBack.getSala()).isNull();
    }
}
