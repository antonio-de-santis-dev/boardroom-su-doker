package main.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import main.IntegrationTest;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Focused tests for the /api/eventis/pubblici endpoint.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventiResourceTest {

    private static final String PUBLIC_API_URL = "/api/eventis/pubblici";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventiRepository eventiRepository;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    private Prenotazioni prenotazioneConfermata;

    @BeforeEach
    void setup() {
        initStatiPrenotazione();
        prenotazioneConfermata = createConfirmedPrenotazione();
    }

    @Test
    @Transactional
    void getPublicEventi_shouldReturnOnlyPublicConfirmedEvents() throws Exception {
        Eventi publicEvent = new Eventi()
            .titolo("Evento pubblico")
            .tipo(TipoEvento.PUBBLICO)
            .prezzo(BigDecimal.TEN)
            .prenotazione(prenotazioneConfermata);

        eventiRepository.saveAndFlush(publicEvent);

        Eventi privateEvent = new Eventi()
            .titolo("Privato")
            .tipo(TipoEvento.PRIVATO)
            .prezzo(BigDecimal.ONE)
            .prenotazione(prenotazioneConfermata);

        eventiRepository.saveAndFlush(privateEvent);

        mockMvc
            .perform(get(PUBLIC_API_URL).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titolo").value("Evento pubblico"))
            .andExpect(jsonPath("$[0].tipo").value("PUBBLICO"));
    }

    private void initStatiPrenotazione() {
        if (statiPrenotazioneRepository.count() == 0) {
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.WAITING).descrizione("In attesa").ordineAzione(1)
            );
            statiPrenotazioneRepository.saveAndFlush(
                new StatiPrenotazione().codice(StatoCodice.CONFIRMED).descrizione("Confermata").ordineAzione(2)
            );
        }
    }

    private Prenotazioni createConfirmedPrenotazione() {
        Sale sala = new Sale();
        sala.setNome("Sala Test");
        sala.setCapienza(10);
        sala = saleRepository.saveAndFlush(sala);

        Utenti utente = new Utenti();
        utente.setNome("Mario");
        utente.setNumeroDiTelefono("3331234567");
        utente = utentiRepository.saveAndFlush(utente);

        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setSala(sala);
        pren.setUtente(utente);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());

        return prenotazioniRepository.saveAndFlush(pren);
    }
}
