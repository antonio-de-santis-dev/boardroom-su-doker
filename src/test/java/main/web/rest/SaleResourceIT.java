package main.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.SaleDTO;
import main.service.mapper.SaleMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SaleResourceIT {

    private static final String DEFAULT_NOME = "Sala A";
    private static final String UPDATED_NOME = "Sala B";
    private static final Integer DEFAULT_CAPIENZA = 10;
    private static final Integer UPDATED_CAPIENZA = 20;

    private static final String ENTITY_API_URL = "/api/sales";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleMapper saleMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    private Sale sala;
    private Sale insertedSala;

    @BeforeEach
    void initTest() {
        sala = new Sale();
        sala.setNome(DEFAULT_NOME);
        sala.setCapienza(DEFAULT_CAPIENZA);
    }

    @AfterEach
    void cleanup() {
        if (insertedSala != null) {
            saleRepository.delete(insertedSala);
            insertedSala = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/sales
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void createSale_shouldPersistEntity() throws Exception {
        long countBefore = saleRepository.count();

        SaleDTO dto = saleMapper.toDto(sala);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME))
            .andExpect(jsonPath("$.capienza").value(DEFAULT_CAPIENZA));

        assertThat(saleRepository.count()).isEqualTo(countBefore + 1);
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/sales/{id}
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void updateSale_shouldUpdateEntity() throws Exception {
        insertedSala = saleRepository.saveAndFlush(sala);

        SaleDTO dto = saleMapper.toDto(insertedSala);
        dto.setNome(UPDATED_NOME);
        dto.setCapienza(UPDATED_CAPIENZA);

        mockMvc
            .perform(
                put(ENTITY_API_URL + "/" + insertedSala.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value(UPDATED_NOME))
            .andExpect(jsonPath("$.capienza").value(UPDATED_CAPIENZA));

        Sale updated = saleRepository.findById(insertedSala.getId()).orElseThrow();
        assertThat(updated.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(updated.getCapienza()).isEqualTo(UPDATED_CAPIENZA);
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/sales
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void getAllSale_shouldReturnList() throws Exception {
        insertedSala = saleRepository.saveAndFlush(sala);

        mockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(insertedSala.getId().toString())))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].capienza").value(hasItem(DEFAULT_CAPIENZA)));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/sales/disponibili
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void getDisponibili_shouldReturnSale_whenNoPrenotazione() throws Exception {
        insertedSala = saleRepository.saveAndFlush(sala);

        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime inizio = LocalTime.of(10, 0);
        LocalTime fine = LocalTime.of(11, 0);

        mockMvc
            .perform(
                get(ENTITY_API_URL + "/disponibili")
                    .param("data", data.toString())
                    .param("inizio", inizio.toString())
                    .param("fine", fine.toString())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(hasItem(insertedSala.getId().toString())));
    }

    @Test
    @Transactional
    void getDisponibili_shouldNotReturnSala_whenPrenotazioneConfirmedOverlaps() throws Exception {
        initStatiPrenotazione();
        insertedSala = saleRepository.saveAndFlush(sala);

        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime inizio = LocalTime.of(10, 0);
        LocalTime fine = LocalTime.of(11, 0);

        // Prenotazione CONFIRMED che occupa la sala nello stesso slot
        Prenotazioni pren = new Prenotazioni();
        pren.setData(data);
        pren.setOraInizio(inizio);
        pren.setOraFine(fine);
        pren.setSala(insertedSala);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());
        prenotazioniRepository.saveAndFlush(pren);

        mockMvc
            .perform(
                get(ENTITY_API_URL + "/disponibili")
                    .param("data", data.toString())
                    .param("inizio", inizio.toString())
                    .param("fine", fine.toString())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(org.hamcrest.Matchers.not(hasItem(insertedSala.getId().toString()))));
    }

    @Test
    @Transactional
    void getDisponibili_shouldFilterByCapienza_whenProvided() throws Exception {
        // Sala grande (30 posti)
        Sale salaGrande = new Sale();
        salaGrande.setNome("Sala Grande");
        salaGrande.setCapienza(30);
        saleRepository.saveAndFlush(salaGrande);

        // Sala piccola (5 posti) — NON deve comparire se capienza richiesta è 15
        insertedSala = saleRepository.saveAndFlush(sala); // DEFAULT_CAPIENZA = 10

        LocalDate data = LocalDate.now().plusDays(1);

        mockMvc
            .perform(
                get(ENTITY_API_URL + "/disponibili")
                    .param("data", data.toString())
                    .param("inizio", "09:00")
                    .param("fine", "10:00")
                    .param("capienza", "15")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(hasItem(salaGrande.getId().toString())))
            .andExpect(jsonPath("$.[*].id").value(org.hamcrest.Matchers.not(hasItem(insertedSala.getId().toString()))));

        saleRepository.delete(salaGrande);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/sales/{id}
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void deleteSale_shouldRemoveEntity() throws Exception {
        insertedSala = saleRepository.saveAndFlush(sala);
        long countBefore = saleRepository.count();

        mockMvc
            .perform(delete(ENTITY_API_URL + "/" + insertedSala.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertThat(saleRepository.count()).isEqualTo(countBefore - 1);
        insertedSala = null;
    }

    @Test
    @Transactional
    void deleteSale_shouldReturnNoContent_whenSalaDoesNotExist() throws Exception {
        mockMvc
            .perform(delete(ENTITY_API_URL + "/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    // ─────────────────────────────────────────────────────────────
    // helper
    // ─────────────────────────────────────────────────────────────

    private void initStatiPrenotazione() {
        if (statiPrenotazioneRepository.count() > 0) return;

        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.WAITING).descrizione("In attesa").ordineAzione(1)
        );
        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.CONFIRMED).descrizione("Confermata").ordineAzione(2)
        );
        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.REJECTED).descrizione("Rifiutata").ordineAzione(3)
        );
        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.CANCELLED).descrizione("Annullata").ordineAzione(4)
        );
    }
}
