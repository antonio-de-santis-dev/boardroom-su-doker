package main.web.rest;

import static main.domain.EventiAsserts.*;
import static main.domain.enumeration.TipoEvento.PUBBLICO;
import static main.web.rest.TestUtil.createUpdateProxyForBean;
import static main.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
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
import main.service.dto.EventiDTO;
import main.service.mapper.EventiMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link EventiResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventiResourceIT {

    private static final String DEFAULT_TITOLO = "AAAAAAAAAA";
    private static final String UPDATED_TITOLO = "BBBBBBBBBB";

    private static final TipoEvento DEFAULT_TIPO = TipoEvento.PRIVATO;
    private static final TipoEvento UPDATED_TIPO = PUBBLICO;

    private static final BigDecimal DEFAULT_PREZZO = new BigDecimal(1);
    private static final BigDecimal UPDATED_PREZZO = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/eventis";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventiRepository eventiRepository;

    @Autowired
    private EventiMapper eventiMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    private Eventi eventi;
    private Eventi insertedEventi;

    public static Eventi createEntity() {
        return new Eventi().titolo(DEFAULT_TITOLO).tipo(DEFAULT_TIPO).prezzo(DEFAULT_PREZZO);
    }

    public static Eventi createUpdatedEntity() {
        return new Eventi().titolo(UPDATED_TITOLO).tipo(UPDATED_TIPO).prezzo(UPDATED_PREZZO);
    }

    @BeforeEach
    void initTest() {
        eventi = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEventi != null) {
            eventiRepository.delete(insertedEventi);
            insertedEventi = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD base
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void createEventi_shouldPersistEntity() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        EventiDTO returnedEventiDTO = objectMapper.readValue(
            mockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(eventiDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EventiDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);

        Eventi returnedEventi = eventiMapper.toEntity(returnedEventiDTO);
        assertEventiUpdatableFieldsEquals(returnedEventi, getPersistedEventi(returnedEventi));

        insertedEventi = returnedEventi;
    }

    @Test
    @Transactional
    void createEventiWithExistingId_shouldReturnBadRequest() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        long databaseSizeBeforeCreate = getRepositoryCount();

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitoloIsRequired_shouldFailOnNull() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        eventi.setTitolo(null);

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTipoIsRequired_shouldFailOnNull() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        eventi.setTipo(null);

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEventis_shouldReturnList() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        mockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventi.getId().toString())))
            .andExpect(jsonPath("$.[*].titolo").value(hasItem(DEFAULT_TITOLO)))
            .andExpect(jsonPath("$.[*].tipo").value(hasItem(DEFAULT_TIPO.toString())))
            .andExpect(jsonPath("$.[*].prezzo").value(hasItem(sameNumber(DEFAULT_PREZZO))));
    }

    @Test
    @Transactional
    void getEventi_shouldReturnSingleEntity() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        mockMvc
            .perform(get(ENTITY_API_URL_ID, eventi.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(eventi.getId().toString()))
            .andExpect(jsonPath("$.titolo").value(DEFAULT_TITOLO))
            .andExpect(jsonPath("$.tipo").value(DEFAULT_TIPO.toString()))
            .andExpect(jsonPath("$.prezzo").value(sameNumber(DEFAULT_PREZZO)));
    }

    @Test
    @Transactional
    void getNonExistingEventi_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────
    // update() — BUG FIX: tipo non deve diventare NULL
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void putExistingEventi_shouldUpdateEntity() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        Eventi updatedEventi = eventiRepository.findById(eventi.getId()).orElseThrow();
        entityManager.detach(updatedEventi);
        updatedEventi.titolo(UPDATED_TITOLO).tipo(UPDATED_TIPO).prezzo(UPDATED_PREZZO);

        EventiDTO eventiDTO = eventiMapper.toDto(updatedEventi);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventiDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEventiToMatchAllProperties(updatedEventi);
    }

    @Test
    @Transactional
    void putExistingEventi_shouldPreserveTipo_whenTipoNotSentByFrontend() throws Exception {
        // Questo test verifica il fix del bug: tipo=NULL → 500 error
        insertedEventi = eventiRepository.saveAndFlush(eventi); // PRIVATO

        EventiDTO dtoSenzaTipo = new EventiDTO();
        dtoSenzaTipo.setId(eventi.getId());
        dtoSenzaTipo.setTitolo("Titolo Aggiornato Senza Tipo");
        dtoSenzaTipo.setTipo(null); // frontend non invia tipo

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventi.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dtoSenzaTipo))
            )
            .andExpect(status().isOk());

        // Il tipo deve essere rimasto PRIVATO — NON NULL
        Eventi persisted = eventiRepository.findById(eventi.getId()).orElseThrow();
        assertThat(persisted.getTipo()).isEqualTo(TipoEvento.PRIVATO);
        assertThat(persisted.getTitolo()).isEqualTo("Titolo Aggiornato Senza Tipo");
    }

    @Test
    @Transactional
    void putExistingEventi_shouldSetPrezzoZero_whenSwitchingToPrivato() throws Exception {
        // Evento inizialmente pubblico con prezzo
        eventi.setTipo(TipoEvento.PUBBLICO);
        eventi.setPrezzo(BigDecimal.valueOf(50));
        insertedEventi = eventiRepository.saveAndFlush(eventi);

        EventiDTO dto = eventiMapper.toDto(eventiRepository.findById(eventi.getId()).orElseThrow());
        dto.setTipo(TipoEvento.PRIVATO); // cambio a privato

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, dto.getId()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto))
            )
            .andExpect(status().isOk());

        Eventi persisted = eventiRepository.findById(eventi.getId()).orElseThrow();
        assertThat(persisted.getTipo()).isEqualTo(TipoEvento.PRIVATO);
        assertThat(persisted.getPrezzo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    void putNonExistingEventi_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventiDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEventi_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEventi_shouldReturnMethodNotAllowed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isMethodNotAllowed());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void partialUpdateEventiWithPatch_shouldUpdateSelectedFields() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        Eventi partialUpdatedEventi = new Eventi();
        partialUpdatedEventi.setId(eventi.getId());
        partialUpdatedEventi.titolo(UPDATED_TITOLO);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventi.getId())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(partialUpdatedEventi))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventiUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedEventi, eventi), getPersistedEventi(eventi));
    }

    @Test
    @Transactional
    void fullUpdateEventiWithPatch_shouldUpdateAllUpdatableFields() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        Eventi partialUpdatedEventi = new Eventi();
        partialUpdatedEventi.setId(eventi.getId());
        partialUpdatedEventi.titolo(UPDATED_TITOLO).tipo(UPDATED_TIPO).prezzo(UPDATED_PREZZO);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventi.getId())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(partialUpdatedEventi))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventiUpdatableFieldsEquals(partialUpdatedEventi, getPersistedEventi(partialUpdatedEventi));
    }

    @Test
    @Transactional
    void patchNonExistingEventi_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, eventiDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEventi_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(eventiDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEventi_shouldReturnMethodNotAllowed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventi.setId(UUID.randomUUID());

        EventiDTO eventiDTO = eventiMapper.toDto(eventi);

        mockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(objectMapper.writeValueAsBytes(eventiDTO)))
            .andExpect(status().isMethodNotAllowed());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void deleteEventi_shouldRemoveEntity() throws Exception {
        insertedEventi = eventiRepository.saveAndFlush(eventi);
        long databaseSizeBeforeDelete = getRepositoryCount();

        mockMvc
            .perform(delete(ENTITY_API_URL_ID, eventi.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    // ─────────────────────────────────────────────────────────────
    // /crea-pubblico — eventi pubblici
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void createPublicEvent_shouldSetPublicTypeAndPrice() throws Exception {
        initStatiPrenotazione();

        Sale sala = createSala("Sala Test", 10);
        Utenti utente = createUtente("Mario", "3331234567");

        Prenotazioni prenotazione = createConfirmedPrenotazione(sala, utente);

        EventiDTO dto = new EventiDTO();
        dto.setPrenotazioneId(prenotazione.getId());
        dto.setTitolo("Concerto");
        dto.setTipo(PUBBLICO);
        dto.setPrezzo(BigDecimal.valueOf(20));

        mockMvc
            .perform(
                post("/api/eventis/crea-pubblico").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo").value("PUBBLICO"))
            .andExpect(jsonPath("$.prezzo").value(20));
    }

    @Test
    @Transactional
    void createPublicEvent_withNonConfirmedBooking_shouldReturnBadRequest() throws Exception {
        initStatiPrenotazione();

        Prenotazioni prenotazione = new Prenotazioni();
        prenotazione.setData(LocalDate.now().plusDays(1));
        prenotazione.setOraInizio(LocalTime.of(10, 0));
        prenotazione.setOraFine(LocalTime.of(11, 0));
        prenotazione.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING).orElseThrow());
        prenotazione = prenotazioniRepository.saveAndFlush(prenotazione);

        EventiDTO dto = new EventiDTO();
        dto.setPrenotazioneId(prenotazione.getId());
        dto.setTitolo("Evento");
        dto.setTipo(PUBBLICO);
        dto.setPrezzo(BigDecimal.valueOf(10));

        mockMvc
            .perform(
                post("/api/eventis/crea-pubblico").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto))
            )
            .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────────
    // /pubblici — lista pubblica senza autenticazione
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void getPublicEvents_shouldReturnOnlyPublicEvents() throws Exception {
        eventi.setTipo(PUBBLICO);
        eventiRepository.saveAndFlush(eventi);

        Eventi privateEvent = new Eventi();
        privateEvent.setTitolo("Privato");
        privateEvent.setTipo(TipoEvento.PRIVATO);
        eventiRepository.saveAndFlush(privateEvent);

        mockMvc
            .perform(get("/api/eventis/pubblici"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].titolo").value(hasItem(DEFAULT_TITOLO)))
            .andExpect(jsonPath("$.[*].titolo").value(not(hasItem("Privato"))));
    }

    @Test
    @Transactional
    void getPublicEvents_shouldBeAccessibleWithoutAuthentication() throws Exception {
        // Questo endpoint è pubblico — accessibile senza login
        mockMvc.perform(get("/api/eventis/pubblici")).andExpect(status().isOk());
    }

    // ─────────────────────────────────────────────────────────────
    // helper methods
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

    private Sale createSala(String nome, int capienza) {
        Sale sala = new Sale();
        sala.setNome(nome);
        sala.setCapienza(capienza);
        return saleRepository.saveAndFlush(sala);
    }

    private Utenti createUtente(String nome, String telefono) {
        Utenti utente = new Utenti();
        utente.setNome(nome);
        utente.setNumeroDiTelefono(telefono);
        return utentiRepository.saveAndFlush(utente);
    }

    private Prenotazioni createConfirmedPrenotazione(Sale sala, Utenti utente) {
        Prenotazioni prenotazione = new Prenotazioni();
        prenotazione.setData(LocalDate.now().plusDays(1));
        prenotazione.setOraInizio(LocalTime.of(10, 0));
        prenotazione.setOraFine(LocalTime.of(11, 0));
        prenotazione.setSala(sala);
        prenotazione.setUtente(utente);
        prenotazione.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());
        return prenotazioniRepository.saveAndFlush(prenotazione);
    }

    protected long getRepositoryCount() {
        return eventiRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Eventi getPersistedEventi(Eventi eventi) {
        return eventiRepository.findById(eventi.getId()).orElseThrow();
    }

    protected void assertPersistedEventiToMatchAllProperties(Eventi expectedEventi) {
        assertEventiAllPropertiesEquals(expectedEventi, getPersistedEventi(expectedEventi));
    }

    protected void assertPersistedEventiToMatchUpdatableProperties(Eventi expectedEventi) {
        assertEventiAllUpdatablePropertiesEquals(expectedEventi, getPersistedEventi(expectedEventi));
    }
}
